### 概念篇

#### RPC是什么？

RPC是远程过程调用 (Remote Procedure Call) 的简称，用于解决分布式系统中多个服务之间的的互相调用问题。通俗来说就是 “让开发者像调用本地方法一样调用远程的服务”，它屏蔽了远程调用和本地调用的区别，并且隐藏了底层复杂的网络通信，让开发者可以更加专注于业务逻辑。

### 实战篇

前言

许多项目一上来就直接讲RPC框架的技术选型，项目架构，通信流程等等， 很容易劝退新人。本项目将先从基本的RPC调用开始，一步步地不断为其拓展功能，使其逐步从一个小瘦子变成大胖子。

#### 小试牛刀：RPC调用

![rpc调用](https://img-blog.csdnimg.cn/87a0f64c5da6417caf569feabb137ffa.png)

##### 请求消息与响应消息

调用本地服务 (接口)，首先是new一个实现类对象，再调用该对象的某个具体方法，传入参数，得到返回结果。RPC调用一样的套路，客户端向服务端传递一个请求消息（RpcRequestMessage），里面包含了要调用服务的全限定名（serviceName）、方法名（methodName）、参数类型（parameterTypes）、参数值（parameterValues），服务端再返回一个响应结果（returnValue），出现异常的话则返回异常信息（exceptionMessage）。请求和响应消息共同继承一个Message 父类，其属性有序列化算法编号（serializerId）、消息序列号（sequenceId），消息序列号作用就是保证一条请求消息对应一条响应消息。

##### 自定义消息协议、编解码

正如HTTP协议用于浏览器和服务器通信一样，RPC调用自然也少不了客户端和服务端的通信，通信双方事先商量好规则，彼此知道发过来的消息该如何解析。

- 自定义消息协议

  ![自定义消息协议](https://img-blog.csdnimg.cn/062d3b743dab40309ab022f3c855c848.png)

  - 魔数：通信双方协商的一个暗号，通常采用固定的几个字节表示。魔数的作用是防止任何人随便向服务器的端口上发送数据。 例如 java Class 文件开头就存储了魔数 0xCAFEBABE，在加载 Class 文件时首先会验证魔数的正确性；
  - 协议版本号：随着业务需求的变化，协议可能需要对结构或字段进行改动，不同版本的协议对应的解析方法也是不同的；
  - 序列化算法：序列化算法字段表示数据发送方应该采用何种方法将请求的对象转化为二进制，以及如何再将二进制转化为对象，如 JSON、Hessian、Protostuff、Java 自带序列化等；
  - 消息类型： 在不同的业务场景中，报文可能存在不同的类型。RPC 框架中有请求、响应、心跳等类型的报文；
  - 消息序列号：请求唯一ID，通过这个ID将请求和响应关联起来，确保一个请求对应一个响应；
  - 数据长度：标明数据的长度，用于判断是否是一个完整的数据包；
  - 数据内容：请求体内容

- 编解码

  消息在客户端与服务端的通信定是以字节的形式传递 (Netty将ByteBuffer升级为ByteBuf)，自定义编码器通过继承Netty的 MessageToMessageCodec 类来实现消息编解码。至于为什么不采用 ByteToMessageCodec后面再分析。

  - 什么是编解码？

    编码：通过实现父类的encode方法，消息出站时将其按照自定义协议转换成Bytebuf才能向外传输

    解码：通过实现父类的decode方法，消息入站时将Bytebuf按照自定义协议还原成一条完整消息供Netty进行下一步处理。这里又牵扯到TCP的粘包和半包问题

  - 什么是TCP的粘包和半包？

    TCP 传输协议是面向流的，没有数据包界限，也就是说消息无边界。客户端向服务端发送数据时，可能将一个完整的报文拆分成多个小报文进行发送，也可能将多个报文合并成一个大的报文进行发送；同理，服务端收到的可能是多个小报文拼接的大报文 ，也可能是不完整的报文。

    粘包：abc  def —> abcdef  

    原因：Negle算法；滑动窗口足够大且接收方处理不及时；应用层Bytebuf设置太大

    半包：abcdef —> ab  cdef  

    原因：要发送数据超出MSS限制；滑动窗口剩余大小放不下完整数据；应用层Bytebuf小于实际数据发送量

  - 如何解决TCP的粘包和半包？

    短链接：客户端每发送一次请求就断开与服务端的连接，从建立连接到释放连接之间的数据即为一条完整数据，简单但效率低下。

    固定消息长度 (FixedLengthFrameDecoder) ：接收方与发送方约定一个固定的消息长度，当接收方累计读取到固定长度的报文后，就认为已经获得一个完整的消息。当发送方的数据小于固定长度时，则需要空位补齐。优点是使用简单，缺点也非常明显，无法很好设定固定长度的值，如果长度太大会造成字节浪费，长度太小又会影响消息传输，所以在一般情况下消息定长法不会被采用。

    特定分隔符 (如LineBasedFrameDecoder) ：在每次发送报文的尾部加上特定分隔符，接收方就可以根据特殊分隔符进行消息拆分。 分隔符的选择一定要避免和消息体中字符相同，以免冲突。否则可能出现错误的消息拆分。比较推荐的做法是将消息进行编码，例如 base64 编码，然后可以选择 64 个编码字符之外的字符作为特定分隔符，因为要遍历字符找分隔符故效率也不高。

    消息长度 ( + 消息类型 ) + 消息内容 (LengthFieldBasedFrameDecoder)：
    项目开发中最常用的一种协议，接收方根据消息长度来读取消息内容。前面说的自定义编解码器继承MessageToMessageCodec而不用 ByteToMessageCodec 是因为后者禁止其子类使用@Sharable注解修饰，本意是由编码者自己划分消息边界，因此必须不能被多个channel所共享。而自己重写的encode和decode方法并没有共享变量，我想让其被多个channel共享，正因如此我使用了LengthFieldBasedFrameDecoder + MessageToMessageCodec 的入站handler组合，让前者先帮我划分出一条完整消息，再交由后者处理。

##### 序列化与反序列化

序列化：把对象转换为字节序列的过程称为对象的序列化。byte[] serialize(T obj)
反序列化：把字节序列恢复为对象的过程称为对象的反序列化。T deserialize(Class clazz, byte[] bytes)

目前可供选择的序列化方式有很多，我这里挑了三种来实现：Java原生、Protostuff、Hessian（不要问我为什么不实现 Json，问题实在是太多了，踩过很多坑也浪费了很多时间最终决定放弃，例如八大基本数据类型的序列化、Bigdecimal、集合 ......）

##### 服务端代码

netty 代码是固定的，需要做的就是给SocketChannel添加多个入站和出站handler，但要注意handler 的顺序不能弄错，入站是从前往后、出站是从后往前。

```java
public static void main(String[] args) {
    NioEventLoopGroup boss = new NioEventLoopGroup();
    NioEventLoopGroup worker = new NioEventLoopGroup(); 
	LoggingHandler LOGGING_HANDLER = new LoggingHandler();
    MessageCodec MESSAGE_CODEC = new MessageCodec();
    RpcRequestHandler RPC_REQUEST_HANDLER = new RpcRequestHandler();

    try {
        Channel channel = new ServerBootstrap()
                .group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new ProtocolFrameDecoder());
                        ch.pipeline().addLast(LOGGING_HANDLER);
                        ch.pipeline().addLast(MESSAGE_CODEC);
                        ch.pipeline().addLast(RPC_REQUEST_HANDLER);
                    }
                })
                .bind(Config.getServerPort())
                .sync()
                .channel();
        channel.closeFuture().sync();
    } catch (InterruptedException e) {
        e.printStackTrace();
    } finally {
        boss.shutdownGracefully();
        worker.shutdownGracefully();
    }
}
```

ProtocolFrameDecoder继承自前面的 LengthFieldBasedFrameDecoder（粘包半包处理器） ，MessageCodec（自定义协议编解码器）前面也分析过，整个流程也就是将客户端Channel发来的Bytebuf先处理完粘包半包拆解出一条完整ByteBuf，再解码成RpcRequestMessage，最后交由RpcRequestHandler通过反射调用方法得到返回值，写出RpcResponseMessage，编解码器再将其编码成ByteBuf发送回客户端Channel。

```java
@ChannelHandler.Sharable
@Slf4j
public class RpcRequestHandler extends SimpleChannelInboundHandler<RpcRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequestMessage request) throws Exception {
        RpcResponseMessage response = new RpcResponseMessage();
        response.setSequenceId(request.getSequenceId());
        response.setSerializerId(request.getSerializerId());
        response.setMessageType(RPCRESPONSE);

        try {
            Object service = serviceCacheMap.get(request.getServiceName());
            Method method = service.getClass().getMethod(request.getMethodName(), request.getParameterTypes());
            Object res = method.invoke(service, request.getParameterValues());
            response.setReturnValue(res);
        } catch (Exception e) {
            log.error("远程调用出错: ", e);
            response.setExceptionMessage(e.getCause().toString());
        }
        ctx.writeAndFlush(response);
    }
}
```

##### 客户端代码

客户端要实现的重点在于动态代理和线程通信，既然不是调用本地服务，那肯定不能采用跟之前一样new对象的方式（本地根本没有服务实现类），而是采用JDK动态代理的方式，先拿到代理对象后续每次执行方法都调用InvokeHandler来向服务端发送请求，可是响应结果怎么获得？Promise ! 

首先要说明 netty 中的 Future 与 jdk 中的 Future 同名，但是是两个接口，netty 的 Future 继承自 jdk 的 Future，而 Promise 又对 netty Future 进行了扩展

* jdk Future 只能同步等待任务结束（或成功、或失败）才能得到结果
* netty Future 可以同步等待任务结束得到结果，也可以异步方式得到结果，但都是要等任务结束
* netty Promise 不仅有 netty Future 的功能，而且脱离了任务独立存在，只作为两个线程间传递结果的容器



Map< Promise> 就像一个信箱，消息序列号就作为信箱中各个信封室，Netty线程负责送信，用户线程负责取信，信的内容就是正常或异常返回结果。

```java
private static <T> T getProxyService(Class<T> clazz) {
    T proxyInstance = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, (proxy, method, args) -> {
        RpcRequestMessage message = new RpcRequestMessage();
        message.setSequenceId(autoIncrementId.incrementAndGet());
        message.setMessageType(RPCREQUEST);
        message.setServiceName(clazz.getName());
        message.setMethodName(method.getName());
        message.setParameterTypes(method.getParameterTypes());
        message.setParameterValues(args);
        Channel channel = getChannel();
        DefaultPromise promise = new DefaultPromise(channel.eventLoop());
        promiseMap.put(message.getSequenceId(), promise);
        channel.writeAndFlush(message);

        promise.await();
        if (promise.isSuccess()) {
            return promise.getNow();
        } else {
            throw promise.cause();
        }
    });
    return proxyInstance;
}
```

```java
@ChannelHandler.Sharable
public class RpcResponseHandler extends SimpleChannelInboundHandler<RpcResponseMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponseMessage msg) throws Exception {
        Promise promise = promiseMap.remove(msg.getSequenceId());
        if (msg.getReturnValue() != null) {
            promise.setSuccess(msg.getReturnValue());
        } else {
            promise.setFailure(new RpcException("远程调用失败，被调用方异常信息: " + msg.getExceptionMessage()));
        }
    }
}
```



#### 大显身手：RPC框架

![RPC框架](https://img-blog.csdnimg.cn/653df9e3ac2e4a588f47cace714f6d1d.png)

##### 模块依赖

项目结构

![项目结构](https://img-blog.csdnimg.cn/93d251bdb00a47f49a87577f5aa2b6d5.png)

依赖关系图

![项目依赖关系图](https://img-blog.csdnimg.cn/1cb8af7db73c4e799dcc0cbc76d18608.png)

##### 注册中心

一个服务可能有多个服务提供方（集群），比如HelloService，可能有127.0.0.1:9091, 127.0.0.1:9092, 127.0.0.1:9093三个服务端供客户端访问，注册中心要干的工作就是把这些服务实例注册到注册中心上去，供后续客户端发现并调用。

注册中心可以选择zookeeper、nacos、eureka ...... 我这里选择的是zookeeper（第三方依赖 curator-x-discovery 基于curator实现了服务发现功能，可以很方便的用于此项目），当然这是可以替换的，只需要让其实现以下接口就行。

```java
interface RegisterService {

    void register(ServiceInfo serviceInfo) throws Exception;

    void unRegister(ServiceInfo serviceInfo) throws Exception;

    void destroy() throws IOException;
}

interface DiscoveryService {

    ServiceInfo discovery(String serviceName, LoadBalance loadBalance) throws Exception;

}
```

注：ServiceInfo是服务信息，包含服务名、Netty服务端的地址和端口、服务权重（后面再添加，用于负载均衡）

比如我这里启动了两个服务提供方的springboot程序，他们的端口号分别是8081和8082

他们内置的Netty服务端端口号分别是9091和9092（这点要注意区分）

![springboot服务](https://img-blog.csdnimg.cn/853426ab01504a72b2016d417bceb865.png)

在zkCli中可以看到我现在有两个服务CalculateService和HelloService，其中HelloService有两个服务实例，详细记载了服务名、Netty服务端的地址、端口，后续服务调用方就可以拉取该服务列表并根据服务实例的信息去访问Netty服务端进行RPC调用并得到结果

![zk预览](https://img-blog.csdnimg.cn/8815212558cc42f7bf9b823fa4c763cb.png)

##### 自定义注解

- 服务端 @RpcService（版本号和负载均衡权重后面再谈，此处先忽略不看）

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface RpcService {

    // 要被实现的服务的类对象
    Class<?> serviceClass() default void.class;

    // 要被实现的服务的类的全限定名
    String serviceName() default "";

    // 版本号
    String version() default "1.0";

    // 负载均衡权重
    int weight() default 1;
}
```

该注解加在服务提供方所提供的服务实现类上，示例：

```java
@RpcService
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String name) {
        return "你好，" + name;
    }
}
```

因为@RpcService注解被@Component标注，所以被其标注的服务实现类会由spring工厂创建一个单例对象。我们为该注解提供了一个后置处理器 RpcServiceBeanPostProcessor ，让其在被该注解标注的所有的bean初始化完成之后把服务注册到注册中心，并将这个bean缓存起来，供后续 netty的RpcRequestHandler使用。

```java
@Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        RpcService annotation = beanClass.getAnnotation(RpcService.class);
        if (annotation != null) {
            try {
                Class<?> serviceClass = null;
                if (annotation.serviceClass() != void.class) {
                    serviceClass = annotation.serviceClass();
                } else if (!"".equals(annotation.serviceName())) {
                    serviceClass = Class.forName(annotation.serviceName());
                } else { // 啥都没给就根据其实现的接口类型进行推断
                    Class<?>[] interfaces = beanClass.getInterfaces();
                    for (Class<?> interfaceClass : interfaces) {
                        if (beanClass.getSimpleName().toLowerCase().contains(interfaceClass.getSimpleName().toLowerCase())) {
                            serviceClass = interfaceClass;
                            break;
                        }
                    }
                }
                if (serviceClass == null) {
                    throw new ClassNotFoundException("服务实现类 " + beanClass.getName() + " 未提供接口");
                }
                String serviceName = serviceClass.getName();
                // 将该服务实现类的单例对象放入缓存中，以便后续 RpcRequestHandler反射调方法用
                serviceCacheMap.put(serviceName, bean);
                ServiceInfo serviceInfo = new ServiceInfo()
                        .setAddress(Inet4Address.getLocalHost().getHostAddress())
                        .setPort(properties.getPort())
                        .setServiceName(serviceName);
                // 将服务信息注册到注册中心
                registerService.register(serviceInfo);
            } catch (Exception e) {
                log.error("服务注册失败：", e);
            }
        }
        return bean;
    }
```

- 客户端 @RpcAutowired（版本号和负载均衡策略后面再谈，此处先忽略不看）

```java
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RpcAutowired {

    // 版本号
    String version() default "1.0";

    // 负载均衡策略
    String loadbalance() default "roundrobin";
}
```

该注解由服务调用方加在需要调用的服务的属性字段上，示例：

```java
@RestController
public class HelloController {

    @RpcAutowired
    private HelloService helloService;

    @GetMapping("/hello")
    public String hello(@RequestParam("name") String name) {
        return helloService.sayHello(name);
    }
}
```

同样我们为该注解提供了一个后置处理器 RpcAutowiredBeanPostProcessor ，在所有的bean实例化之后初始化之前 寻找被该注解标注的字段，并为该字段进行属性注入，注入的对象就是前面篇章所讲过的JDK动态代理对象，后续将由它为我们发送RPC请求

```java
@Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> clazz = bean.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            RpcAutowired annotation = field.getAnnotation(RpcAutowired.class);
            if (annotation != null) {
                try {
                    field.setAccessible(true);
                    field.set(bean, RpcProxyFactory.getProxy(field.getType(), discoveryService, properties));
                } catch (IllegalAccessException e) {
                    log.error("属性赋值失败", e);
                }
            }
        }
        return bean;
    }
```



##### SpringBoot自动装配

- 为什么用自动装配？

  要做到用户使用我们的RPC框架时尽量少的配置，所以把RPC框架设计成一个starter，用户只要依赖这个starter，把需要修改的配置在application.yml 中添上就ok了。我们总不能让用户每次使用我们的框架都用@ComponentScan或者@Import注解去扫描或添加框架内部的配置类吧。这显然不符合第三方框架的设计理念。

- 为什么要设计成两个starter？

  这个是为了更好的体现出客户端和服务端的概念，服务调用方依赖客户端，服务提供方依赖服务端。毕竟有些服务只会去调用别的服务，自己不会对外暴露服务，我们只需让其依赖client-starter就行；同样有些服务无需去调用其他服务，只是将自己的服务暴漏出去，我们只需让其依赖server-starter就行。如此设计满足最小化依赖。

- 要装配什么东西？

  针对服务端：

  1. 包含netty启动端口和注册中心地址的配置类RpcServerProperties
  2. 服务注册实现类ZookeeperRegisterService
  3. 注解后置处理器RpcServiceBeanPostProcessor
  4. netty服务端 RpcServer

  针对客户端：

  1. 包含序列化、注册中心地址、请求超时时间和重试次数的配置类RpcClientProperties
  2. 服务发现实现类ZookeeperDiscoveryService
  3. 注解后置处理器RpcAutowiredBeanPostProcessor

- 如何实现自动装配？

  这我们就需要从springboot的启动类看起，@SpringBootApplication—>@EnableAutoConfiguration—>@Import(AutoConfigurationImportSelector.class)

  AutoConfigurationImportSelector实现了ImportSelector接口的selectImports方法，一步步跟进源码可知，springboot会去类路径下的META-INF/spring.factories中读取所有的配置类。所以我们只需在文件中配置以下代码，这里我们starter的配置类就生效了。

  ![自动装配](https://img-blog.csdnimg.cn/6930e6901a564654ba710037be1a27e6.png)

##### 负载均衡

当一个服务有多个提供者时，势必要用到负载均衡策略，常见的有随机、轮询、ip哈希、url哈希、一致性哈希......我这里实现了按权重轮询和按权重随机两种，其余算法可以大家自行进行拓展。只需实现以下接口即可

```java
public interface LoadBalance {
    
    ServiceInfo chooseOne(List<ServiceInfo> serviceInfos);
}
```

该功能是如何实现的呢？

服务端@RpcService注解首先要新增一个负载均衡权重字段 weight，在服务注册时不止要携带端口地址了，还要携带权重。以供客户端拉取

客户端首先把所有负载均衡算法实例都注入spring容器，其次在@RpcAutowired注解新增一个负载均衡策略字段 loadbalance，在RpcAutowiredBeanPostProcessor 执行时将该策略实例传给代理对象，代理对象向注册中心进行服务发现时就会用到该策略，从所有服务列表中返回一个netty服务端再发起服务调用。

用户使用：

```java
@RpcService(weight = 3)
public class HelloServiceImpl implements HelloService {
}

@RestController
public class HelloController {
    
    @RpcAutowired(loadbalance = "random")
    private HelloService helloService;
}
```

##### 多版本

可能一个服务发布了多个不同的实现版本，调用方需要根据实际情况有选择地调用。

该功能的实现比较简单，服务端@RpcService注解和客户端@RpcAutowired注解都新增一个版本号version字段。RpcServiceBeanPostProcessor服务注册时服务名不再单纯是全路径名，而是拼接上版本号，同样RpcAutowiredBeanPostProcessor将要调用的版本号也传给代理对象，代理对象拼接全路径名+版本号之后再进行服务发现。

用户使用：

```java
@RpcService(version = "2.0")
public class HelloServiceImpl2 implements HelloService {
    @Override
    public String sayHello(String name) {
        return "你好，" + name + " 我是版本2";
    }
}

@RestController
public class HelloController {

    @RpcAutowired(version = "2.0")
    private HelloService helloService2;
    
    @GetMapping("/hello2")
    public String hello2(@RequestParam("name") String name) {
        return helloService2.sayHello(name);
    }
}
```



### 总结篇

##### 流程与架构

![流程架构](https://img-blog.csdnimg.cn/315af2c0aa0943249fcf30707831fc30.png)

最后我们来梳理一遍整体流程

流程分为三块：服务提供者启动流程、服务消费者启动、调用过程

- 服务提供者启动
  1. 服务提供者 provider 会依赖 rpc-server-spring-boot-starter
  2. ProviderApplication 启动，根据springboot 自动装配机制，RpcServerAutoConfiguration 自动配置生效
  3. RpcServiceBeanPostProcessor 是一个bean后置处理器，会发布服务，将服务元数据注册到ZK上
  4. RpcServer实现了ApplicationRunner接口，在spring容器启动完成后会调用run 方法开启一个 netty 服务
- 服务消费者启动
  1. 服务消费者 consumer 会依赖 rpc-client-spring-boot-starter
  2. ConsumerApplication 启动，根据springboot 自动装配机制，RpcClientAutoConfiguration 自动配置生效
  3. 将服务发现、负载均衡、代理等bean加入IOC容器
  4. 后置处理器 RpcAutowiredBeanPostProcessor 会扫描 bean ,将被 @RpcAutowired 修饰的属性动态赋值为代理对象
- 调用过程
  1. 服务消费者 发起请求 [http://localhost:8080/hello?name=张三](https://link.juejin.cn?target=http%3A%2F%2Flocalhost%3A9090%2Fhello%2Fworld%3Fname%3Dhello)
  2. 服务消费者 调用 helloService.sayHello() 方法，会被代理到执行 InvocationHandler的invoke() 方法
  3. 服务消费者 通过ZK服务发现获取服务元数据，找不到报错404
  4. 服务消费者 自定义协议，封装请求头和请求体
  5. 服务消费者 通过自定义编码器将请求消息编码
  6. 服务消费者 通过 服务发现获取到服务提供者的ip和端口， 通过Netty网络传输层发起调用
  7. 服务消费者 通过 Promise 进入返回结果（超时）等待
  8. 服务提供者 收到消费者请求
  9. 服务提供者 将消息通过自定义解码器解码
  10. 服务提供者 解码之后的数据发送到 RpcRequestHandler 中进行处理，通过反射调用执行服务端本地方法并获取结果
  11. 服务提供者 将执行的结果通过 编码器将消息编码
  12. 服务消费者 将消息通过自定义解码器解码
  13. 服务消费者 通过RpcResponseHandler将消息写入 请求和响应池中，并设置 Promise 的响应结果
  14. 服务消费者 获取到结果

##### 亟待完善

- 成熟的 RPC 框架一般会提供四种调用方式，分别为同步 Sync、异步 Future、回调 Callback和单向 Oneway。我这里实现了第一种同步调用，其他的没有实现
- 服务调用方从注册中心拉取下来的服务列表应有一份本地缓存，之间然后监听节点的变化去更新本地缓存（心跳），各负载均衡算法也是从缓存中返回服务节点
- 集群容错机制，客户端向一个服务端节点发起调用失败后，应从别的节点进行重试
- ......



