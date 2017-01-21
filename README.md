# pact-jvm-provider-spring-mockmvc

Similar to [realestate-com-au/pact-jvm-provider-spring-mvc](https://github.com/realestate-com-au/pact-jvm-provider-spring-mvc) this projects provides functionality
to write provider pact tests with [spring mock mvc](http://docs.spring.io/spring-security/site/docs/current/reference/html/test-mockmvc.html). 

The whole functionality is wrapped within a JUnit TestRule to write pact provider tests with minimal effort. 

## How to use it

Write a Spring Web Mvc Test, integrate the `PactMockMvcRule` and configure it. The rule will automatically perform and validate the request described in the pact.

Example:
```java
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = MyController.class)
public class PactCdcTest {

    @Rule
    public PactMockMvcRule pactRule = PactMockMvcRule.create().withFile("file:../pacts/my-pact.json").build();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MyRepository repositoryMock;

    @Test
    @ProviderState("I have an entity with ID 10")
    public void provideEntityWithId10() throws Exception {
        pactRule.configure().mockMvc(mockMvc).contextPath("/app");
        
        MyEntity entity = new MyEntity();
        entity.setName("Name");
        entity.setId(10);
        when(repositoryMock.findOne(10)).thenReturn(entity);
    }

    @TestConfiguration
    public static class TestConfig {
        @Bean
        MyRepository mockRepo() {
            return mock(MyRepository.class);
        }
    }
}
```

## @RunWith(SpringRunner.class)

Use the test runner provided by Spring to run your tests. This will evaluate the spring specific annotations and bootstrap springs application context.
Of course you can youse any test runner you like, but using the `SpringRunner` makes things simple.

## @WebMvcTest(controllers = MyController.class)

This enables Springs Web MVC tests and applies configuration relevant to MVC tests. The controllers property specifies the controllers to run the test against.
If Spring Security is enabled it can be easily disabled for testing by setting the `secure` flag to `false`:

```java
@WebMvcTest(controllers = MyController.class, secure = false)
```

## @Rule PactMockMvcRule

Create the `PactMockMvcRule` by using the builder: `PactMockMvcRule.create()`

The builder has some convenient methods to define where the pact files are located:

* `withFile(String pactFile)` - Point to a single pact file.
* `withAllFrom(String pactFile)` - Point to a folder with several pact files. Pact files in sub-folders will be considered as well.

Additionally you can filter the pacts for a certain consumer / provider using `forConsumer(String consumer)` / `forProvider(String provider)`.

Finally call `build()` to get the rule.

## @Autowired MockMvc

Get the `MockMvc` configured by Spring and ready to use. The above defined controllers can be called via rest by this object. 

## @ProviderState("I have an entity with ID 10")

Each test method should be annotated with `@ProviderState` defining the provider state to get the correct consumer - provider interaction from the pacts.
Alternatively the provider state can be directly set on the rule instead of using the annotation. See
[Pact Runner API](#pactRule.configure().mockMvc(mockMvc).contextPath("/app")) for more details.

## @InteractionDescription("A request to retrtieve the value of entity 10")

A test method can additionally be annotated with `@InteractionDescription` to select a defined interaction by its description.
Alternatively the interaction to select can be directly set on the rule instead of using the annotation. See
[next section](#pactRule.configure().mockMvc(mockMvc).contextPath("/app")) for more details.

## pactRule.configure().mockMvc(mockMvc).contextPath("/app")

Within the test method some test specifics must be defined. Call `pactRule.configure()` to configure the next test run. Following configurations are required to
allow the pact test to be executed:

* `mockMvc(MockMvc mockMvc)` - Gives the `MockMvc` to the pact test executor to perform the calls.
* `providerState(String providerState)` - If not already specified by the `@ProviderState` annotation on the test method it can be directly set with this method. The value set here has precedence over the value from the annotation.

Following things can optionally be configured:
 
* `contextPath(String contextPath)` - If the application is deployed under a certain context path this must be specified to ignore that part of the request path.
* `consumer(String consumer)` and `provider(String provider)` - If not already specified on rule creation, the loaded pacts can be narrowed down to the specified consumer and provider.
* `requestCallback(Consumer<? super MockHttpServletRequestBuilder> requestCallback)` - The request can be modified before it is executed.
* `responseCallback(Consumer<? super ResultActions> responseCallback)` - Get access to the response before it is validated.
* `addResultMatchers(ResultMatcher... resultMatchers)` - Provide some additional `ResultMatcher` that will be validated against the response.
* `interactionDescription(String description)` - If not already specified by the `@InteractionDescription` annotation on the test method it can be directly set with this method. The value set here has precedence over the value from the annotation.

Configurations that are common for all tests within a test class (mostly at least the `mockMvc` configuration) can be put to a Before-Method:

```java
@Before
public void setup() {
    pactRule.configure().mockMvc(mockMvc).contextPath("/app");
}
```

## @TestConfiguration

Provide the required mocks for your services, that will be used by spring. Within the test method they can be configured to behave as required.
