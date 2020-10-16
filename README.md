# zhi-rpc-framework
zhi-rpc-framework 是一款基于 Netty+Kryo+Zookeeper 实现的 RPC 框架。代码注释详细，结构清晰，并且集成了 Check Style 规范代码结构，非常适合阅读和学习。
## 介绍
**我们先从一个基本的 RPC 框架设计思路说起！**
### 一个基本的RPC框架设计思路
一个典型的使用 RPC 的场景如下，一般情况下 RPC 框架不仅要提供服务发现功能，还要提供负载均衡、容错等功能，这个的 RPC 框架才算真正合格。
![一个完整的RPC框架使用示意图](https://my-blog-to-use.oss-cn-beijing.aliyuncs.com/2019-11/一个完整的rpc框架.jpg)

简单说一下设计一个最基本的 RPC 框架的思路：

1. **注册中心** ：注册中心首先是要有的，推荐使用  Zookeeper。注册中心主要用来保存相关的信息比如远程方法的地址。
2. **网络传输** ：既然要调用远程的方法就要发请求，请求中至少要包含你调用的类名、方法名以及相关参数吧！推荐基于NIO 的 Netty框架。
3. **序列化** ：既然涉及到网络传输就一定涉及到序列化，你不可能直接使用 JDK 自带的序列化吧！JDK 自带的序列化效率低并且有安全漏洞。 所以，你还要考虑使用哪种序列化协议，比较常用的有 hession2、kyro、protostuff。
4. **动态代理** ： 另外，动态代理也是需要的。因为 RPC 的主要目的就是让我们调用远程方法像调用本地方法一样简单，使用动态代理屏蔽远程接口调用的细节比如网络传输。
5. **负载均衡** ：负载均衡也是需要的。为啥？举个例子我们的系统中的某个服务的访问量特别大，我们将这个服务部署在了多台服务器上，当客户端发起请求的时候，多台服务器都可以处理这个请求。那么，如何正确选择处理该请求的服务器就很关键。假如，你就要一台服务器来处理该服务的请求，那该服务部署在多台服务器的意义就不复存在了。负载均衡就是为了避免单个服务器响应同一请求，容易造成服务器宕机、崩溃等问题，我们从负载均衡的这四个字就能明显感受到它的意义。
6. ......
### 项目基本情况和可优化点

最初的时候，我是基于传统的 **BIO** 的方式 **Socket** 进行网络传输，然后利用 **JDK 自带的序列化机制** 以及内存直接存储相关服务相关信息来实现这个 RPC 框架的。

后面，我对原始版本进行了优化，已完成的优化点和可以完成的优化点如下。
**为什么要把可优化点列出来？**  主要是想给哪些希望优化这个 RPC 框架的小伙伴一点思路。欢迎大家 Clone 本仓库，然后自己进行优化。

- [x] **使用Netty（基于NIO）替代BIO实现网络传输；**
- [x] **增加Netty进行客户端连接服务端时的重试机制。**
- [x] **使用开源的序列化机制 Kryo（也可以用其它的）替代 JDK 自带的序列化机制；**
- [x] **使用Zookeeper管理相关服务地址信息**
- [ ] **增加可配置比如序列化方式、注册中心的实现方式,避免硬编码** ：通过API配置，后续集成 Spring 的话建议使用配置文件的方式进行配置
- [ ] **客户端调用远程服务的时候进行负载均衡** ：发布服务的时候增加 一个 loadbalance 参数即可。
- [ ] **使用注解进行服务配置和消费**
- [ ] **处理一个接口有多个实现的情况** ：对服务分组，发布服务的时候增加一个 group 参数即可。
- [ ] **增加服务版本号** ：建议使用两位数字版本，如：1.0，通常在接口不兼容时版本号才需要升级。为什么要增加服务版本号？为后续不兼容升级提供可能，比如服务接口增加方法，或服务模型增加字段，可向后兼容，删除方法或删除字段，将不兼容，枚举类型新增字段也不兼容，需通过变更版本号升级。
- [ ] **客户端与服务端通信协议（数据包结构）重新设计** ，可以将原有的  `RpcRequest `和 `RpcReuqest`  对象作为消息体，然后增加如下字段（可以参考：《Netty入门实战小册》和Dubbo框架对这块的设计）：
  - **魔数** ： 通常是 4 个字节。这个魔数主要是为了筛选来到服务端的数据包，有了这个魔数之后，服务端首先取出前面四个字节进行比对，能够在第一时间识别出这个数据包并非是遵循自定义协议的，也就是无效数据包，为了安全考虑可以直接关闭连接以节省资源。
  - **序列化器编号** ：标识序列化的方式，比如是使用 Java 自带的序列化，还是 json，kyro 等序列化方式。
  - **消息体长度** ： 运行时计算出来。
  - ......
