# README

本项目用于记录学习imooc课程 [企业级Android应用架构设计与开发](https://coding.imooc.com/class/364.html) 的学习笔记

### 学习方式：

* 看课程视频
* 下载课程代码，用 **Sublime merge** 查看各个章节的修改，关键模块的代码自己手动敲一遍
* 每章学完后进行总结，总结的时候思考怎样在简历上体现以及面试问到后怎样回答

### 项目价值

* 学习音乐APP的开发思路和方法
* 熟悉常见的开源库的使用：OkHttp，Glide，RxJava，EventBus
* 丰富简历上的项目经历，可以着重介绍各个组件的开发过程和方法



### lib_network

lib_network 组件是对网络请求的封装，目的是为不同的业务提供统一的网络请求接口，避免各个业务自己实现，减少代码冗余，同时也是为了让业务模块能够与第三方的网络框架解耦，让业务不依赖特定的第三方组件。

**技术选型：**

由于目前Android领域已经有了非常成熟的网络框架，我们这次是针对优秀网络框架的二次封装。最终选择的是**OkHttp**， 因为它是目前最主流的网络框架，而且具有以下特点：

* 功能齐全
* 高性能
* 代码优秀，各种设计模式的运用（Singleton，Builder，策略模式，拦截器模式等等）

**封装思路：**

一次完整的网络请求主要包含三个部分：请求，响应和发送。因此我们的封装也是按照这三个部分来实现的。

* CommonRequest：封装 get/post/文件上传请求的构建
* CommonResponse：封装JsonCallback，供UI层获取数据和更新界面；封装OkHttpException，提高定位效率；封装FileCallback，处理文件下载请求并实现下载进度更新和断点续传
* OkHttpClient：封装一个单例，用于发送post/get/文件下载请求并做一些通用的设置，例如校验hostname，添加公共请求头，设置超时时间等等，后续可以灵活扩展，通过添加setter方法让业务根据需要设置参数。

### lib_image_loader
lib_image_loader 组件是对图片加载功能的封装，为各业务模块提供统一的图片加载体验，并解耦业务模块对第三方库的依赖。

**技术选型：**

常见的图片加载库有 Volley， Picasso，Glide。

* Volley：Volley是 Google推出的库，但是近些年发展不太好。
* Picasso：Picasso 是非常流行的图片加载框架，对内存的优化非常好，它会绑定Activity的声明周期，当Activity销毁时回收图片内存；而且Picasso支持链式调用，使用很方便，会使代码看上去十分简洁。
* Glide：Glide 和 Picasso是同一家公司出品，是目前最流行的图片加载库，也得到了Google的推荐。Glide是基于 Picasso的，因此它继承了Picasso所有的优点，并且更加强大。

因此我们最终选择 Glide 作为我们底层的图片加载框架。

**封装思路：**

使用策略模式，定义ImageLoader策略的接口 `BaseImageLoaderStrategy` ，这里定义了一个ImageLoader要实现的功能，主要有以下几个：

* 为普通ImageView加载图片
* 为ViewGroup加载图片 -- 大图加载优化，用RxJava将耗时部分放到IO线程执行，完成后回调主线程设置图片资源
* 加载圆形图片
* 为Notification的RemoteViews加载图片
* 针对Glide使用Application Context加载出现的内存泄漏问题做了针对性优化（待补充）

由于我们选择的开源框架是Glide，那么就用Glide实现一个类，它实现了前面定义的策略接口。后续如果想要替换成其它的库，只需要用替换的库实现相同的接口即可。
实际对外提供图片加载能力的是 `ImageLoader`类，它默认使用 `GlideImageLoaderStrategy` 作为加载策略，可以灵活替换，并且提供设置默认策略的接口，用户自己实现加载策略。

这样设计带来的好处就是可扩展性非常好，如果将来要替换成其它的库所做的改动很小，而且业务完全不感知。












