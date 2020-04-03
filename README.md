
[![Build](https://travis-ci.com/skhatri/mounted-secrets-client.svg?branch=master)](https://travis-ci.com/github/skhatri/mounted-secrets-client)
[![Code Coverage](https://img.shields.io/codecov/c/github/skhatri/mounted-secrets-client/master.svg)](https://codecov.io/github/skhatri/mounted-secrets-client?branch=master)

## Mounted Secrets Client

Mounted Secrets Client is a java library that reads various secrets from one or many mounts and provides a simple lookup method to retrieve
them.

This library can be embedded in java apps which have the need to read passwords injected by hashicorp vault's vault-injector or some other similar 
password providers. If it is stored in file, it can read it.


### How do I use it?

Add the following to your gradle file

```
implementation("io.github.skhatri:mounted-secrets-client:0.2.4")
```

### Example?

```
Map<String, SecretProvider> secretProviderMap = new HashMap<>();
SecretProvider secretProvider = SecretProviders.anyForName("vault");
secretProviderMap.put("vault", secretProvider);
SecretConfiguration config = new SecretConfiguration();
config.setProviders(Arrays.asList(secretProvider));
config.setKeyErrorDecision("identity");
MountedSecretsFactory factory = new MountedSecretsFactory(config);
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
secrets:
  enabled: true
  config:
    #fail, empty, identity. defaults to fail
    key-error-decision: "fail"
    providers:
    - name: vault
      mount: "${SECRETS_FILE:/vault/secrets}"
      key: "${KEY_FILE:/tmp/key}"
      #if default is not set at entry level, fail, empty, identity. defaults to fail
      error-decision: empty
      #additional entries in one file, uri
      entries-location: "others.properties"
```

The attributes are explained below:

| Configuration | Description |
| ------------- |:------------|
| enabled | Flag to enable/disable secret lookup. Uses NoOp function if disabled  |
| config:         |           |
| ` `key-error-decision | Flag to provide instructions for when the key is not well-formed.  |
| ` `provider:       |             |
| `  `name            | Namespace to support multiple secret mounts            |
| `  `mount           | Top Level Resource directory where content is placed   |
| `  `key             | Unused. Possibly used for decryption/client-auth later |
| `  `error-decision  | Instructions on what the error handling strategy is at field level for this namespace |
| `  `entries-location| Used for mounts where keys are stored in properties file format | 



#### Step 2. Load ProviderList from config

Create a ConfigurationProperties bean
```
@ConfigurationProperties(prefix="secrets")
public class SecretsProperties {
    private SecretConfiguration config;

    public SecretConfiguration getConfig() {
        return providers;
    }

    public void setConfig(SecretConfiguration config) {
        this.config = config;
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
    public MountedSecretsResolver newResolver(SecretsProperties secretProperties) {
          return new MountedSecretsFactory(secretProperties.getConfig()).create();
    }
}
```

Then inject MountedSecretsResolver where you need it.


### Future Extensions
There is FileSystemResolver. It can be extended to support Encrypted Mounted Files. The mount could also be an URL. 
 
 