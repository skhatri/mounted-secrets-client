
[![Build](https://travis-ci.com/skhatri/mounted-secrets-client.svg?branch=master)](https://travis-ci.com/github/skhatri/mounted-secrets-client)

## Mounted Secrets Client

Mounted Secrets Client is a java library that reads various secrets from one or many mounts and provides a simple lookup method to retrieve
them.

This library can be embedded in java apps which have the need to read passwords injected by hashicorp vault's vault-injector or some other similar 
password providers. If it is stored in file, it can read it.


### How do I use it?

Add the following to your gradle file

```
implementation("com.github.skhatri:mounted-secrets:1.0.1")
```

### Example?

```
Map<String, SecretProvider> secretProviderMap = new HashMap<>();
SecretProvider secretProvider = SecretProviders.anyForName("vault");
secretProviderMap.put("vault", secretProvider);
ProviderList providerList = new ProviderList(Arrays.asList(secretProvider));
MountedSecretsFactory factory = new MountedSecretsFactory(providerList);
secretsResolver = factory.create();


SecretValue secretValue = secretsResolver.resolve("secret::vault::key1");
if (secretValue.isFailure()) {
  ...
}
Optional<String> value = secretValue.getValue();


SecretValue secretValue = secretsResolver.resolve("secret::vault::key_xyz:-some value");
Optional<String> value = secretValue.getValue();
assert secretValue.hasValue();
```

### How do I configure Spring?

#### Step 1. Add a map like this in application.yaml:
```
secrets.providers:
  - name: vault
    mount: "${SECRETS_FILE:/vault/secrets}"
    key: "${KEY_FILE:/tmp/key}"
    #if default is not set at entry level, fail, empty, identity
    error-decision: empty
    #additional entries in one file, uri
    entries-location: "others.properties"
```

#### Step 2. Load ProviderList from config

Create a ConfigurationProperties bean
```
@ConfigurationProperties(prefix="secrets")
public class SecretProviderConfig {
    private List<SecretProvider> providers;

    public List<SecretProvider> getProviders() {
        return providers;
    }

    public void setProviders(List<SecretProvider> providers) {
        this.providers = providers;
    }

}

```

#### Step 3. Create a MountedSecretsResolver

Instantiate a MountedSecretsResolver like this

```
@Configuration
public class SecretResolverConfig {
    @Bean
    @Autowired
    public MountedSecretsResolver newResolver(SecretProviderConfig secretProviderConfig) {
          ProviderList providerList = new ProviderList(secretProviderConfig.getProviders);
          return new MountedSecretsFactory(providerList).factory.create();
    }
}
```

Then inject MountedSecretsResolver where you need it.


### Future Extensions
There is FileSystemResolver. It can be extended to support Encrypted Mounted Files. The mount could also be an URL. 
 
 