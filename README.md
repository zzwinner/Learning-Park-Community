# Learning-Park-Community
# 学乐园-大学生在线学习交流社区
学乐园是一个大学生在线学习交流社区，作为本人学习springboot后第一个练手项目，随着不断地深化学习会进一步更新代码版本
<br/>
<h2>已经实现的功能：</h2>
<h3>1、主页（讨论帖页）的帖子展示和分页</h3>
<h3>2、通过springboot向指定用户发送邮件</h3>
<h4>注：使用springboot向用户发送邮件的功能实现：</h4>
<b>第一步，</b>在个人邮箱设置中开启SMTP服务，QQ邮箱会给一个服务授权码，保存好；<br/>
<b>第二步，</b>在idea中引入springboot-mail的jar包<br/>
<b>第三步，</b>在配置文件中配置好mail，配置内容如下：<br/>
spring.mail.host=smtp.qq.com--邮箱服务器地址(个人)<br/>
spring.mail.port=465--端口号<br/>
spring.mail.username=xxxx@qq.com--发送邮件的邮箱<br/>
spring.mail.password=eomklgxlgcekfiad--QQ邮箱中开启SMTP服务的授权码<br/>
spring.banner.charset=utf-8--支持中文邮件<br/>
spring.mail.protocol=smtps--安全发送邮件<br/>
spring.mail.properties.mail.smtp.ssl.enable=true--使用SSL协议发送邮件，并安全发送<br/>
<b>第四步，</b>在项目目录下新建util工具类，并新建发送邮件的工具类<br/>
<b>第五步，</b>如果需要发送HTML邮件，需要使用thymeleaf模板<br/>
<h3>3、用户注册功能以及向用户发送激活邮件进行激活操作</h3>
<h3>4、通过kaptcha生成随机验证码图片</h3>
<h3>5、用户登录、退出登录以及根据登录状态显示不同页面内容</h3>
