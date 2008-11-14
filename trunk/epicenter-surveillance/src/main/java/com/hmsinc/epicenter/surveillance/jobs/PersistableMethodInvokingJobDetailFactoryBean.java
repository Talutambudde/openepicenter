/**
 * Copyright (C) 2008 University of Pittsburgh
 * 
 * 
 * This file is part of Open EpiCenter
 * 
 *     Open EpiCenter is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     Open EpiCenter is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with Open EpiCenter.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 *   
 */
package com.hmsinc.epicenter.surveillance.jobs;

import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.StatefulJob;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.MethodInvoker;

/**
 * @see http://forum.springframework.org/showthread.php?t=27275
 * @version $Id$
 */
public class PersistableMethodInvokingJobDetailFactoryBean extends MethodInvokingJobDetailFactoryBean {

	private String targetBeanName;

	private String targetMethod;

	private Object[] arguments;

	private String name;

	private String group = Scheduler.DEFAULT_GROUP;

	private boolean concurrent = true;

	private String beanName;

	private JobDetail jobDetail;

	private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

	/**
	 * Set the name of the job. Default is the bean name of this FactoryBean.
	 * 
	 * @see org.quartz.JobDetail#setName
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Set the group of the job. Default is the default group of the Scheduler.
	 * 
	 * @see org.quartz.JobDetail#setGroup
	 * @see org.quartz.Scheduler#DEFAULT_GROUP
	 */
	public void setGroup(String group) {
		this.group = group;
	}

	/**
	 * Specify whether or not multiple jobs should be run in a concurrent
	 * fashion. The behavior when one does not want concurrent jobs to be
	 * executed is realized through adding the {@link StatefulJob} interface.
	 * More information on stateful versus stateless jobs can be found <a
	 * href="http://www.opensymphony.com/quartz/tutorial.html#jobsMore">here</a>.
	 * <p>
	 * The default setting is to run jobs concurrently.
	 * 
	 * @param concurrent
	 *            whether one wants to execute multiple jobs created by this
	 *            bean concurrently
	 */
	public void setConcurrent(boolean concurrent) {
		this.concurrent = concurrent;
	}

	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	public void setBeanClassLoader(ClassLoader classLoader) {
		this.beanClassLoader = classLoader;
	}

	protected Class<?> resolveClassName(String className) throws ClassNotFoundException {
		return ClassUtils.forName(className, this.beanClassLoader);
	}

	public void afterPropertiesSet() throws ClassNotFoundException, NoSuchMethodException {
		
		// Use specific name if given, else fall back to bean name.
		String name = (this.name != null ? this.name : this.beanName);

		// Consider the concurrent flag to choose between stateful and stateless
		// job.
		Class<?> jobClass = (this.concurrent ? (Class<?>) PersistableMethodInvokingJob.class : StatefulPersistableMethodInvokingJob.class);

		this.jobDetail = new JobDetail(name, this.group, jobClass);
		this.jobDetail.getJobDataMap().put("targetBeanName", targetBeanName);
		this.jobDetail.getJobDataMap().put("targetMethod", targetMethod);
		if (arguments != null) {
			this.jobDetail.getJobDataMap().put("arguments", arguments);
		}
	}

	public Object getObject() {
		return this.jobDetail;
	}

	/**
	 * @return the arguments
	 */
	public Object[] getArguments() {
		return arguments;
	}

	/**
	 * @param arguments
	 *            the arguments to set
	 */
	public void setArguments(Object[] arguments) {
		this.arguments = arguments;
	}

	/**
	 * @return the targetBeanName
	 */
	public String getTargetBeanName() {
		return targetBeanName;
	}

	/**
	 * @param targetBeanName
	 *            the targetBeanName to set
	 */
	public void setTargetBeanName(String targetBeanName) {
		this.targetBeanName = targetBeanName;
	}

	/**
	 * @return the targetMethod
	 */
	public String getTargetMethod() {
		return targetMethod;
	}

	/**
	 * @param targetMethod
	 *            the targetMethod to set
	 */
	public void setTargetMethod(String targetMethod) {
		this.targetMethod = targetMethod;
	}

	/**
	 * Quartz Job implementation that invokes a specified method.
	 */
	public static class PersistableMethodInvokingJob extends QuartzJobBean {
		
		private String targetMethod;
		private String targetBeanName;
		private Object[] arguments;

		/**
		 * @param targetMethod the targetMethod to set
		 */
		public void setTargetMethod(String targetMethod) {
			this.targetMethod = targetMethod;
		}

		/**
		 * @param targetBeanName the targetBeanName to set
		 */
		public void setTargetBeanName(String targetBeanName) {
			this.targetBeanName = targetBeanName;
		}

		/**
		 * @param arguments the arguments to set
		 */
		public void setArguments(Object[] arguments) {
			this.arguments = arguments;
		}


		// Dont want to build a proxy each time - too slow
		protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
			try {
				ApplicationContext appContext = (ApplicationContext) context.getScheduler().getContext().get("applicationContext");
				Assert.state(appContext != null, "Application context was null!");
				
				Assert.state(targetBeanName != null, "Target bean name was null!");
				
				Object targetBean = appContext.getBean(targetBeanName);
				Assert.state(appContext != null, "Target bean was null! [" + targetBeanName + "]");
				
				MethodInvoker methodInvoker = new MethodInvoker();
				methodInvoker.setTargetObject(targetBean);
				methodInvoker.setTargetMethod(targetMethod);
				
				if (arguments != null) {
					methodInvoker.setArguments(arguments);
				}
				methodInvoker.prepare();

				methodInvoker.invoke();
				
			} catch (Exception ex) {
				String errorMessage = "Could not invoke method '" + targetMethod + "' on target bean [" + targetBeanName + "]";
				throw new JobExecutionException(errorMessage, ex, false);
			}
		}
	}

	/**
	 * Extension of the PersistableMethodInvokingJob , implementing the
	 * StatefulJob interface. Quartz checks whether or not jobs are stateful and
	 * if so, won't let jobs interfere with each other.
	 */
	public static class StatefulPersistableMethodInvokingJob extends PersistableMethodInvokingJob implements StatefulJob {
		// No implementation,
	}
}
