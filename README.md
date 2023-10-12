# ly-mall
## 项目介绍

- ly 商城是一个全品类的电商购物网站（B2C）。
- 用户可以在线购买商品、加入购物车、下单、秒杀商品
- 可以品论已购买商品   // TODU
- 管理员可以在后台管理商品的上下架、促销活动
- 管理员可以监控商品销售状况

## 系统架构

整个 ly 商城可以分为两部分：后台管理系统、前台门户系统。

- 后台管理：

  - 后台系统主要包含以下功能：
    - 商品管理，包括商品分类、品牌、商品规格等信息的管理
    - 销售管理，包括订单统计、促销活动生成等
    - 用户管理，包括用户信息管理
    - 权限管理，整个网站的权限控制，采用JWT鉴权方案，对用户及API进行权限控制
    - 统计，各种数据的统计分析展示

  - 后台系统采用前后端分离开发，整个后台管理系统使用Vue.js框架搭建单页应用（SPA）。

- 前台门户

  - 前台门户面向的是客户，包含与客户交互的一切功能。例如：
    - 搜索商品
    - 查看详情
    - 加入购物车
    - 下单
    - 秒杀
    - 支付
  - 前台系统使用Nuxt结合Vue完成页面开发。

无论是前台还是后台系统，都共享相同的微服务集群，包括：

- 商品微服务：商品及商品分类、品牌、库存等的服务
- 搜索微服务：实现搜索功能
- 订单微服务：实现订单相关
- 秒杀微服务：实现秒杀功能
- 购物车微服务：实现购物车相关功能
- 用户中心：用户的登录注册等功能
- 认证中心：用户权限及服务权限认证
- Eureka注册中心
- Zuul网关服务
- ...

## 相关技术

前端技术：

- 基础的HTML、CSS、JavaScript（基于ES6标准）
- Vue.js 2.0以及基于Vue的UI框架：Vuetify
- 前端构建工具：WebPack
- 前端安装包工具：NPM
- Vue脚手架：Vue-cli
- Vue路由：vue-router
- ajax框架：axios
- 基于Vue的富文本框架：quill-editor

后端技术：

- 基础的SpringMVC、Spring 5.0和MyBatis3
- Spring Boot 2.0.1版本
- Spring Cloud  Finchley.RC1版本
- Redis-4.0
- RabbitMQ-3.4
- Elasticsearch-5.6.8
- nginx-1.10.2
- FastDFS - 5.0.8
- MyCat
- Thymeleaf
- JWT

## 技术解读

上面的技术组合可以在项目中解决以下电商中的典型问题：

- 利用Node.js及Vue.js技术栈，实现前后端分离开发
- 利用SpringCloud技术栈，实现微服务实战开发
- 贴近真实的电商数据库设计，解决全品类电商的SPU和SKU管理问题
- 基于FastDFS解决大数据量的分布式文件存储问题
- 基于Elasticsearch高级聚合功能，实现商品的智能过滤搜索
- 基于LocalStorage实现离线客户端购物车，减轻服务端压力。
- 基于JWT技术及RSA非对称加密实现真正无状态的单点登录。
- 基于阿里大于实现SMS功能，解决电商短信通知问题
- 基于RabbitMQ实现可靠消息服务，解决服务间通信问题
- 基于RabbitMQ实现可靠消息服务，解决分布式事务问题
- 使用微信SDK实现微信扫码支付，符合主流付款方式
- 基于Thymeleaf实现页面模板和静态化，提高页面响应速度和并发能力
- 基于Nginx实现初步的请求负载均衡和请求限流

## 说明

项目仅供个人学习使用。
