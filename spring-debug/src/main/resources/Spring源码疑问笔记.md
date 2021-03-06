### 1、Spring Aop注解与xml配置文件执行切面方法的顺序不一致

	Aop的注解配置生成advisor排序与xml配置文件生成的Advisor排序不一致的问题，差异在xml文件解析的Advisor, Before会在Arorund前执行，而基于注解的则Around在Before之前执行。（此现象可能会Spring版本不同，而有差异）

1、基于注解的排序看 ：ReflectiveAspectJAdvisorFactory

```
	private static final Comparator<Method> METHOD_COMPARATOR;

	static {
		Comparator<Method> adviceKindComparator = new ConvertingComparator<>(
				new InstanceComparator<>(
						Around.class, Before.class, After.class, AfterReturning.class, AfterThrowing.class),
				(Converter<Method, Annotation>) method -> {
					AspectJAnnotation<?> ann = AbstractAspectJAdvisorFactory.findAspectJAnnotationOnMethod(method);
					return (ann != null ? ann.getAnnotation() : null);
				});
		Comparator<Method> methodNameComparator = new ConvertingComparator<>(Method::getName);
		METHOD_COMPARATOR = adviceKindComparator.thenComparing(methodNameComparator);
	}
	
	
	private List<Method> getAdvisorMethods(Class<?> aspectClass) {
  		final List<Method> methods = new ArrayList<>();
  		// ReflectionUtils.MethodCallback的匿名实现
  		ReflectionUtils.doWithMethods(aspectClass, method -> {
  			// Exclude pointcuts
  			// 声明为Pointcut的方法不处理
  			if (AnnotationUtils.getAnnotation(method, Pointcut.class) == null) {
  				methods.add(method);
  			}
  		}, ReflectionUtils.USER_DECLARED_METHODS);
  		//在这里调用排序
  		if (methods.size() > 1) {
  			methods.sort(METHOD_COMPARATOR);
  		}
  		return methods;
	}
```

2、基于Xml的Advisor排序 ：AspectJAwareAdvisorAutoProxyCreator

```
protected List<Advisor> sortAdvisors(List<Advisor> advisors) {
		//创建一个排序封装类
		List<PartiallyComparableAdvisorHolder> partiallyComparableAdvisors = new ArrayList<>(advisors.size());
		for (Advisor advisor : advisors) {
			//有点没有弄明白，当对象是advisor时，order的值是在哪赋值的，找了一圈没有找到。
			partiallyComparableAdvisors.add(
					new PartiallyComparableAdvisorHolder(advisor, DEFAULT_PRECEDENCE_COMPARATOR));
		}
		//进行排序
		List<PartiallyComparableAdvisorHolder> sorted = PartialOrder.sort(partiallyComparableAdvisors);
		if (sorted != null) {
			List<Advisor> result = new ArrayList<>(advisors.size());
			for (PartiallyComparableAdvisorHolder pcAdvisor : sorted) {
				result.add(pcAdvisor.getAdvisor());
			}
			return result;
		}
		else {
			return super.sortAdvisors(advisors);
		}
	}
```

<br/>

### 2、原型bean的获取

 每次getBean都会通过RootBeanDefinition的定义生成一个新的bean

### 3、Bean的scope=session、 Request 的获取

 在Spring 内部有一个接口Scope,下面有多个实现类，分别有SessionScope，RequestScope，在这些实现类中通过ThreadLocal维护Bean与线程的关系。

### 4、Spring bean子类继承了父类，子类与父类如何进行实例化

 需要看父类是配置状态，正常情况下在spring容器里，如果实例化子类时，不会去对父类进行实例化（也没有必要），如果需要先实例化父类可以使用@DependsOn注解，并将父类要加入到Spring容器管理

### 5、Aware类的使用

https://www.jianshu.com/p/3c7e0608ff1f

### 6、lookup-method  replace-method 的覆盖

 BeanDefinition中的methodOverrides属性记录lookup-method,replaced-method的信息。在实例化时判断bd.hasMethodOverrides()进入此方法，实例一个代理对象，在方法被调用时，通过拦截器找到对应关系，进而实现方法的替换。

```java
public Object instantiate(@Nullable Constructor<?> ctor, Object... args) {
			// 根据beanDefinition来创建一个cglib的子类
			Class<?> subclass = createEnhancedSubclass(this.beanDefinition);
			Object instance;
			// 如果构造器等于空，那么直接通过反射来实例化对象
			if (ctor == null) {
				instance = BeanUtils.instantiateClass(subclass);
			}
			else {
				try {
					// 通过cglib对象来根据参数类型获取对应的构造器
					Constructor<?> enhancedSubclassConstructor = subclass.getConstructor(ctor.getParameterTypes());
					// 通过构造器来获取对象
					instance = enhancedSubclassConstructor.newInstance(args);
				}
				catch (Exception ex) {
					throw new BeanInstantiationException(this.beanDefinition.getBeanClass(),
							"Failed to invoke constructor for CGLIB enhanced subclass [" + subclass.getName() + "]", ex);
				}
			}
			Factory factory = (Factory) instance;
			factory.setCallbacks(new Callback[] {NoOp.INSTANCE,
					new LookupOverrideMethodInterceptor(this.beanDefinition, this.owner),
					new ReplaceOverrideMethodInterceptor(this.beanDefinition, this.owner)});
			return instance;
		}
```

### 7、如果没有三级缓存会怎么样

	如果没有三级缓存，在使用Aop时可能会报错，对象的实例化过程是分为几个阶段的，每个阶段存放到各个缓存中

阶段1：BeanInstance(实例化) -> 本身应该存放在三级缓存，但此时如果三级不存在，则存放二级缓存，key=beanName value=实例化后的bean.

阶段2：populateBean（填充属性） 

阶段3：initializeBean（调用初始化方法，同时会判断是否需要生成代理，如果需要则返回的是代理bean）-> 存放到一级缓存

	如果没有三级缓存，此时的问题就出现了。如果initializeBean后，exposedObject=返回的是代理对象，不等于BeanInstance时存在二级缓存中的原始Bean实例（earlySingletonReference） 然后 !this.allowRawInjectionDespiteWrapping && hasDependentBean(beanName) == true,后面就会触发BeanCurrentlyInCreationException。

```java
//AbstractAutowireCapableBeanFactory.class
if (earlySingletonExposure) {
			// 从缓存中获取具体的对象
			Object earlySingletonReference = getSingleton(beanName, false); 
			// earlySingletonReference只有在检测到有循环依赖的情况下才会不为空
			if (earlySingletonReference != null) {
				// 如果exposedObject没有在初始化方法中被改变，也就是没有被增强
				if (exposedObject == bean) { 
					exposedObject = earlySingletonReference;
				}
				else if (!this.allowRawInjectionDespiteWrapping && hasDependentBean(beanName)) {  //
					String[] dependentBeans = getDependentBeans(beanName);
					Set<String> actualDependentBeans = new LinkedHashSet<>(dependentBeans.length);
					for (String dependentBean : dependentBeans) {
						// 返回false说明依赖还没实例化好
						if (!removeSingletonIfCreatedForTypeCheckOnly(dependentBean)) {
							actualDependentBeans.add(dependentBean);
						}
					}
					// 因为bean创建后所依赖的bean一定是已经创建的
					// actualDependentBeans不为空则表示当前bean创建后其依赖的bean却没有全部创建完，也就是说存在循环依赖
					if (!actualDependentBeans.isEmpty()) {
						throw new BeanCurrentlyInCreationException(beanName,
								"Bean with name '" + beanName + "' has been injected into other beans [" +
								StringUtils.collectionToCommaDelimitedString(actualDependentBeans) +
								"] in its raw version as part of a circular reference, but has eventually been " +
								"wrapped. This means that said other beans do not use the final version of the " +
								"bean. This is often the result of over-eager type matching - consider using " +
								"'getBeanNamesForType' with the 'allowEagerInit' flag turned off, for example.");
					}
				}
			}
		}
```

  也有大佬在https://blog.csdn.net/wang489687009/article/details/120655156 在这里有更加深一层的思考，目前还没有去试着重现。

### 8、实例化时构造方法的选择

首先会在AutowiredAnnotationBeanPostProcessor.determineCandidateConstructors中识别所有的构造方法，并对构造方法进行分类优先级的处理。

> 1、如果有@Autowired，required为true，则封装此构造方法为数组进行返回
> 
> 2、如果只存在一个构造函数，且这个构造函数有参数列表，则封装此构造方法为数组进行返回
> 
> 3、如果非合成构造存在两个且有主构造和默认构造，且主构造和默认构造不相等，则封装此构造方法为数组进行返回
> 
> 4、如果只有一个非合成构造且有主构造，则封装此构造方法为数组进行返回
> 
> 5、大于2个构造方法就不知道要用什么了，所以就返回null

得到确认后应该使用的构造方法的数组后，再通过以下方法确定调用哪个构造方法

```java
public BeanWrapper autowireConstructor(String beanName, RootBeanDefinition mbd,
			@Nullable Constructor<?>[] chosenCtors, @Nullable Object[] explicitArgs) {
		// 实例化BeanWrapper。是包装bean的容器
		BeanWrapperImpl bw = new BeanWrapperImpl();
		// 给包装对象设置一些属性
		this.beanFactory.initBeanWrapper(bw);

		// spring对这个bean进行实例化使用的构造函数
		Constructor<?> constructorToUse = null;
		// spring执行构造函数使用的是参数封装类
		ArgumentsHolder argsHolderToUse = null;
		// 参与构造函数实例化过程的参数
		Object[] argsToUse = null;

		// 如果传入参数的话，就直接使用传入的参数
		if (explicitArgs != null) {
			//让argsToUse引用explicitArgs
			argsToUse = explicitArgs;
		}
		// 没有传入参数的话就走else
		else {
			//声明一个要解析的参数值数组，默认为null
			Object[] argsToResolve = null;
			synchronized (mbd.constructorArgumentLock) {
				// 获取BeanDefinition中解析完成的构造函数
				constructorToUse = (Constructor<?>) mbd.resolvedConstructorOrFactoryMethod;
				// BeanDefinition中存在构造函数并且存在构造函数的参数，赋值进行使用
				if (constructorToUse != null && mbd.constructorArgumentsResolved) {
					// Found a cached constructor...
					// 从缓存中找到了构造器，那么继续从缓存中寻找缓存的构造器参数
					argsToUse = mbd.resolvedConstructorArguments;
					if (argsToUse == null) {
						// 没有缓存的参数，就需要获取配置文件中配置的参数
						argsToResolve = mbd.preparedConstructorArguments;
					}
				}
			}
			// 如果缓存中没有缓存的参数的话，即argsToResolve不为空，就需要解析配置的参数
			if (argsToResolve != null) {
				// 解析参数类型，比如将配置的String类型转换为list、boolean等类型
				argsToUse = resolvePreparedArguments(beanName, mbd, bw, constructorToUse, argsToResolve, true);
			}
		}

		//如果constructorToUse为null或者argsToUser为null
		if (constructorToUse == null || argsToUse == null) {
			// Take specified constructors, if any.
			// 如果传入的构造器数组不为空，就使用传入的过后早期参数，否则通过反射获取class中定义的构造器
			Constructor<?>[] candidates = chosenCtors;
			//如果candidates为null
			if (candidates == null) {
				//获取mbd的Bean类
				Class<?> beanClass = mbd.getBeanClass();
				try {
					// 使用public的构造器或者所有构造器
					candidates = (mbd.isNonPublicAccessAllowed() ?
							beanClass.getDeclaredConstructors() : beanClass.getConstructors());
				}
				//捕捉获取beanClass的构造函数发出的异常
				catch (Throwable ex) {
					throw new BeanCreationException(mbd.getResourceDescription(), beanName,
							"Resolution of declared constructors on bean Class [" + beanClass.getName() +
							"] from ClassLoader [" + beanClass.getClassLoader() + "] failed", ex);
				}
			}

			//如果candidateList只有一个元素 且 没有传入构造函数值 且 mbd也没有构造函数参数值
			if (candidates.length == 1 && explicitArgs == null && !mbd.hasConstructorArgumentValues()) {
				//获取candidates中唯一的方法
				Constructor<?> uniqueCandidate = candidates[0];
				//如果uniqueCandidate不需要参数
				if (uniqueCandidate.getParameterCount() == 0) {
					//使用mdb的构造函数字段的通用锁【{@link RootBeanDefinition#constructorArgumentLock}】进行加锁以保证线程安全
					synchronized (mbd.constructorArgumentLock) {
						//让mbd缓存已解析的构造函数或工厂方法
						mbd.resolvedConstructorOrFactoryMethod = uniqueCandidate;
						//让mbd标记构造函数参数已解析
						mbd.constructorArgumentsResolved = true;
						//让mbd缓存完全解析的构造函数参数
						mbd.resolvedConstructorArguments = EMPTY_ARGS;
					}
					//使用constructorToUse生成与beanName对应的Bean对象,并将该Bean对象保存到bw中
					bw.setBeanInstance(instantiate(beanName, mbd, uniqueCandidate, EMPTY_ARGS));
					//将bw返回出去
					return bw;
				}
			}

			// Need to resolve the constructor.
			// 自动装配标识，以下有一种情况成立则为true，
			// 1、传进来构造函数，证明spring根据之前代码的判断，知道应该用哪个构造函数，
			// 2、BeanDefinition中设置为构造函数注入模型
			boolean autowiring = (chosenCtors != null ||
					mbd.getResolvedAutowireMode() == AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR);
			//定义一个用于存放解析后的构造函数参数值的ConstructorArgumentValues对象
			ConstructorArgumentValues resolvedValues = null;

			// 构造函数的最小参数个数
			int minNrOfArgs;
			// 如果传入了参与构造函数实例化的参数值，那么参数的数量即为最小参数个数
			if (explicitArgs != null) {
				//minNrOfArgs引用explitArgs的数组长度
				minNrOfArgs = explicitArgs.length;
			}
			else {
				// 提取配置文件中的配置的构造函数参数
				ConstructorArgumentValues cargs = mbd.getConstructorArgumentValues();
				// 用于承载解析后的构造函数参数的值
				resolvedValues = new ConstructorArgumentValues();
				// 能解析到的参数个数
				minNrOfArgs = resolveConstructorArguments(beanName, mbd, bw, cargs, resolvedValues);
			}

			// 对候选的构造函数进行排序，先是访问权限后是参数个数
			// public权限参数数量由多到少
			AutowireUtils.sortConstructors(candidates);
			// 定义一个差异变量，变量的大小决定着构造函数是否能够被使用
			int minTypeDiffWeight = Integer.MAX_VALUE;
			// 不明确的构造函数集合，正常情况下差异值不可能相同
			Set<Constructor<?>> ambiguousConstructors = null;
			//定义一个用于UnsatisfiedDependencyException的列表
			LinkedList<UnsatisfiedDependencyException> causes = null;

			// 循环候选的构造函数
			for (Constructor<?> candidate : candidates) {
				// 获取参数的个数
				int parameterCount = candidate.getParameterCount();

				// 如果已经找到选用的构造函数或者需要的参数个数小于当前的构造函数参数个数则终止，前面已经经过了排序操作
				if (constructorToUse != null && argsToUse != null && argsToUse.length > parameterCount) {
					// Already found greedy constructor that can be satisfied ->
					// do not look any further, there are only less greedy constructors left.
					break;
				}
				//如果本构造函数的参数列表数量小于要求的最小数量，则遍历下一个
				if (parameterCount < minNrOfArgs) {
					// 参数个数不相等
					continue;
				}

				// 存放构造函数解析完成的参数列表
				ArgumentsHolder argsHolder;
				// 获取参数列表的类型
				Class<?>[] paramTypes = candidate.getParameterTypes();
				// 存在需要解析的构造函数参数
				if (resolvedValues != null) {
					try {
						// 获取构造函数上的ConstructorProperties注解中的参数
						String[] paramNames = ConstructorPropertiesChecker.evaluate(candidate, parameterCount);
						// 如果没有上面的注解，则获取构造函数参数列表中属性的名称
						if (paramNames == null) {
							// 获取参数名称探索器
							ParameterNameDiscoverer pnd = this.beanFactory.getParameterNameDiscoverer();
							if (pnd != null) {
								// 获取指定构造函数的参数名称
								paramNames = pnd.getParameterNames(candidate);
							}
						}
						// 根据名称和数据类型创建参数持有者
						argsHolder = createArgumentArray(beanName, mbd, resolvedValues, bw, paramTypes, paramNames,
								getUserDeclaredConstructor(candidate), autowiring, candidates.length == 1);
					}
					catch (UnsatisfiedDependencyException ex) {
						if (logger.isTraceEnabled()) {
							logger.trace("Ignoring constructor [" + candidate + "] of bean '" + beanName + "': " + ex);
						}
						// Swallow and try next constructor.
						// 吞下并尝试下一个重载的构造函数
						// 如果cause为null
						if (causes == null) {
							//对cause进行实例化成LinkedList对象
							causes = new LinkedList<>();
						}
						//将ex添加到causes中
						causes.add(ex);
						continue;
					}
				}
				// 不存在构造函数参数列表需要解析的参数
				else {
					// Explicit arguments given -> arguments length must match exactly.
					// 如果参数列表的数量与传入进来的参数数量不相等，继续遍历，否则构造参数列表封装对象
					if (parameterCount != explicitArgs.length) {
						continue;
					}
					// 构造函数没有参数的情况
					argsHolder = new ArgumentsHolder(explicitArgs);
				}

				// 计算差异量，根据要参与构造函数的参数列表和本构造函数的参数列表进行计算
				int typeDiffWeight = (mbd.isLenientConstructorResolution() ?
						argsHolder.getTypeDifferenceWeight(paramTypes) : argsHolder.getAssignabilityWeight(paramTypes));
				// Choose this constructor if it represents the closest match.
				// 本次的构造函数差异值小于上一个构造函数，则进行构造函数更换
				if (typeDiffWeight < minTypeDiffWeight) {
					// 将确定使用的构造函数设置为本构造
					constructorToUse = candidate;
					// 更换使用的构造函数参数封装类
					argsHolderToUse = argsHolder;
					// 更换参与构造函数实例化的参数
					argsToUse = argsHolder.arguments;
					// 差异值更换
					minTypeDiffWeight = typeDiffWeight;
					// 不明确的构造函数列表清空为null
					ambiguousConstructors = null;
				}
				// 差异值相等，则表明构造函数不正常，放入异常集合
				else if (constructorToUse != null && typeDiffWeight == minTypeDiffWeight) {
					//如果ambiguousFactoryMethods为null
					if (ambiguousConstructors == null) {
						//初始化ambiguousFactoryMethods为LinkedHashSet实例
						ambiguousConstructors = new LinkedHashSet<>();
						//将constructorToUse添加到ambiguousFactoryMethods中
						ambiguousConstructors.add(constructorToUse);
					}
					//将candidate添加到ambiguousFactoryMethods中
					ambiguousConstructors.add(candidate);
				}
			}

			//以下两种情况会抛异常
			// 1、没有确定使用的构造函数
			// 2、存在模糊的构造函数并且不允许存在模糊的构造函数
			if (constructorToUse == null) {
				if (causes != null) {
					UnsatisfiedDependencyException ex = causes.removeLast();
					for (Exception cause : causes) {
						this.beanFactory.onSuppressedException(cause);
					}
					throw ex;
				}
				throw new BeanCreationException(mbd.getResourceDescription(), beanName,
						"Could not resolve matching constructor " +
						"(hint: specify index/type/name arguments for simple parameters to avoid type ambiguities)");
			}
			else if (ambiguousConstructors != null && !mbd.isLenientConstructorResolution()) {
				throw new BeanCreationException(mbd.getResourceDescription(), beanName,
						"Ambiguous constructor matches found in bean '" + beanName + "' " +
						"(hint: specify index/type/name arguments for simple parameters to avoid type ambiguities): " +
						ambiguousConstructors);
			}

			/**
			 * 没有传入参与构造函数参数列表的参数时，对构造函数缓存到BeanDefinition中
			 * 	1、缓存BeanDefinition进行实例化时使用的构造函数
			 * 	2、缓存BeanDefinition代表的Bean的构造函数已解析完标识
			 * 	3、缓存参与构造函数参数列表值的参数列表
			 */
			if (explicitArgs == null && argsHolderToUse != null) {
				// 将解析的构造函数加入缓存
				argsHolderToUse.storeCache(mbd, constructorToUse);
			}
		}

		Assert.state(argsToUse != null, "Unresolved constructor arguments");
		// 将构造的实例加入BeanWrapper中
		bw.setBeanInstance(instantiate(beanName, mbd, constructorToUse, argsToUse));
		return bw;
	}
```

### 9、@Autowried的使用原理

  看这个得需要先了解Spring的体系，然后知道BeanPostProcessor是干什么的，然后再来看AutowiredAnnotationBeanPostProcessor的流程图。



### 10、Aop同一个类中需要通过代理进行方法调用

通过AopContext.currentProxy() 可以得到代理对象，再进行方法调用，就可以走代理

原理：

在JdkDynamicAopProxy.invoke方法中，有这么一段代码

```java
	/**
			 * 这个配置是暴露我们的代理对象到线程变量中，需要搭配@EnableAspectJAutoProxy(exposeProxy = true)一起使用
			 * 比如在目标对象方法中再次获取代理对象可以使用这个AopContext.currentProxy()
			 * 还有的就是事务方法调用事务方法的时候也是用到这个
			 */
			if (this.advised.exposeProxy) {
				// Make invocation available if necessary.
				// 把我们的代理对象暴露到线程变量中
				oldProxy = AopContext.setCurrentProxy(proxy);
				setProxyContext = true;
			}
			
	static Object setCurrentProxy(@Nullable Object proxy) {
  		Object old = currentProxy.get();
  		if (proxy != null) {
  			currentProxy.set(proxy);
  		}
  		else {
  			currentProxy.remove();
  		}
  		return old;
	}
```



### 11、< scoped-proxy >标签解析

传送门 https://blog.csdn.net/Mr_SeaTurtle_/article/details/52992207

### 12、org.springframework.context.annotation.ConfigurationClassPostProcessor.configurationClass == full 是啥意思

传送门 https://blog.csdn.net/demon7552003/article/details/107988310

### 13、@Lookup 

传送门 https://blog.csdn.net/duxd185120/article/details/109125440

### 14、@lazy的原理

传送门 https://blog.csdn.net/wang489687009/article/details/120577472 

### 15、@Async注解的使用

 传送门 ： https://blog.csdn.net/BryantLmm/article/details/85129372

传送门：https://blog.csdn.net/wang489687009/article/details/122223277

### 16、AOP MethodInterceptor原理

 先欠着

### 17、BeanDefinition的理解

传送门 ：https://blog.csdn.net/wang489687009/article/details/120131856

### 18、@Resource与@Autowired的区别

传送门 : https://blog.csdn.net/wang489687009/article/details/119908896