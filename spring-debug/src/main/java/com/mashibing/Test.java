package com.mashibing;

public class Test {

    public static void main(String[] args) {
//        MyClassPathXmlApplicationContext ac = new MyClassPathXmlApplicationContext("factoryBean.xml");
////        MyFactoryBean bean1 = (MyFactoryBean) ac.getBean( "&myFactoryBean");
////        System.out.println(bean1);
////        User bean = (User) ac.getBean("myFactoryBean");
////        System.out.println(bean.getUsername());
////        User bean2 = (User) ac.getBean("myFactoryBean");
////        System.out.println(bean2.getUsername());
//
////        MyClassPathXmlApplicationContext ac = new MyClassPathXmlApplicationContext("applicationContext.xml");
////        System.out.println(ac.getBean(MyPropertySource.class).getName());
////        ApplicationContext ac = new ClassPathXmlApplicationContext("spring-${username}.xml");
////        Person bean = ac.getBean(Person.class);
////        System.out.println(bean);
////        A bean1 = ac.getBean(A.class);
////        System.out.println(bean1);
////        ac.close();
////        User user = (User) ac.getBean("msb");
////        System.out.println(user.getUsername());
//

//
////        MyClassPathXmlApplicationContext ac = new MyClassPathXmlApplicationContext("factoryMethod.xml");
//
////        ClassPathXmlApplicationContext ac = new ClassPathXmlApplicationContext("test.xml");
////        Person person = ac.getBean("person", Person.class);
////        ac.close();
////        Person person2 = ac.getBean("person", Person.class);
//
////        ApplicationContext ac = new ClassPathXmlApplicationContext("person.xml");
////        Person bean = ac.getBean(Person.class);
////        Person bean2 = ac.getBean(Person.class);
//
////        System.out.println(System.getProperties());
////        System.out.println(System.getenv());
//
//        ApplicationContext ac = new ClassPathXmlApplicationContext("public2.xml");
//        Student bean = ac.getBean(Student.class);
//        System.out.println(bean);

        // 所有的对象必须只有一个，单例对象
//        A a = new A();
//        B b = new B();



        //想通过student获取其他bean对象怎么办？


//        Teacher bean = context.getBean(Teacher.class);
//        System.out.println(bean.getId());
//        System.out.println(bean.getName());
//        System.out.println(bean.getApplicationContext());
//        System.out.println(bean.getBeanFactory());
        testSimpleLoadingProcess();
    }

    /**
     * 第一个例子，测试spring简答的启动 过程
     */
    private  static  void testSimpleLoadingProcess(){
//        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring-${USERNAME}.xml");
//        Student bean = context.getBean(Student.class);
//        context.close();
    }
}
