package org.llw.common.pool;

import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
/**
 * 
 * @author liulw
 *
 */
@Component
@Configuration
@ConfigurationProperties(prefix = "pool")
public class TaskExecutorConfig implements AsyncConfigurer {
	//线程池维护线程的最少数量
	@Value("${pool.corePoolSize:#{5}}")
	private int corePoolSize;
    //允许的空闲时间
	@Value("${pool.keepAliveSeconds:#{200}}")
    private int keepAliveSeconds;
    //线程池维护线程的最大数量
	@Value("${pool.maxPoolSize:#{10}}")
    private int maxPoolSize;
    //缓存队列
	@Value("${pool.queueCapacity:#{400}}")
    private int queueCapacity;
	
    public int getCorePoolSize() {
		return corePoolSize;
	}


	public void setCorePoolSize(int corePoolSize) {
		this.corePoolSize = corePoolSize;
	}


	public int getKeepAliveSeconds() {
		return keepAliveSeconds;
	}


	public void setKeepAliveSeconds(int keepAliveSeconds) {
		this.keepAliveSeconds = keepAliveSeconds;
	}


	public int getMaxPoolSize() {
		return maxPoolSize;
	}


	public void setMaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}


	public int getQueueCapacity() {
		return queueCapacity;
	}


	public void setQueueCapacity(int queueCapacity) {
		this.queueCapacity = queueCapacity;
	}


	@Bean
    public ThreadPoolTaskExecutor getAsyncExecutor() {
        ThreadPoolTaskExecutor threadPool = new ThreadPoolTaskExecutor();
        //设置核心线程数
        threadPool.setCorePoolSize(corePoolSize);
        //设置最大线程数
        threadPool.setMaxPoolSize(maxPoolSize);
        //线程池所使用的缓冲队列
        threadPool.setQueueCapacity(queueCapacity);
        //等待任务在关机时完成--表明等待所有线程执行完
        threadPool.setWaitForTasksToCompleteOnShutdown(true);
        // 等待时间 （默认为0，此时立即停止），并没等待xx秒后强制停止
        threadPool.setAwaitTerminationSeconds(keepAliveSeconds);
        // 初始化线程
        threadPool.initialize();
        threadPool.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return threadPool;
    }
}
