package com.oho.redis.core;

import com.oho.common.enums.RLockEnum;
import com.oho.common.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.RedissonMultiLock;
import org.redisson.RedissonRedLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 基于Redis Redisson 分布式锁工具类
 *
 * @author Sparkler
 * @createDate 2022 /12/21
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RLockUtils {

    private RedissonClient redissonClient;

    /**
     * 获取锁。默认使用可重入锁
     *
     * @param lockKey key
     * @return lock lock
     */
    public RLock getLock(String lockKey) {
        return redissonClient.getLock(lockKey);
    }

    /**
     * 根据传入锁类型获取锁
     *
     * @param lockKey   the lock key
     * @param rLockEnum the r lock enum
     * @return the lock
     */
    public RLock getLock(String lockKey, RLockEnum... rLockEnum) {
        RLockEnum lockType = RLockEnum.ReentrantLock;
        if (ObjectUtils.isNotNull(rLockEnum) && ObjectUtils.isNotEmpty(rLockEnum)) {
            lockType = rLockEnum[0];
        }
        switch (lockType) {
            case FairLock:
                return redissonClient.getFairLock(lockKey);
            case ReadLock:
                return redissonClient.getReadWriteLock(lockKey).readLock();
            case WriteLock:
                return redissonClient.getReadWriteLock(lockKey).writeLock();
            default:
                return getLock(lockKey);
        }
    }

    /**
     * 基于Redis的Redisson分布式联锁[RedissonMultiLock]对象可以将多个`RLock`对象关联为一个联锁，
     * 每个`RLock`对象实例可以来自于不同的Redisson实例。
     * ** 所有的锁都上锁成功才算成功。 **
     *
     * @param locks the locks
     * @return r lock
     */
    public RLock multiLock(RLock... locks) {
        return new RedissonMultiLock(locks);
    }

    /**
     * 基于Redis的Redisson红锁`RedissonRedLock`对象实现了[Redlock]介绍的加锁算法。
     * 该对象也可以用来将多个`RLock`对象关联为一个红锁，每个`RLock`对象实例可以来自于不同的Redisson实例。
     * ** 红锁在大部分节点上加锁成功就算成功。 **
     *
     * @param locks the locks
     * @return the r lock
     */
    public RLock redLock(RLock... locks) {
        return new RedissonRedLock(locks);
    }

    /**
     * 加锁
     *
     * @param lockKey   key
     * @param rLockEnum the r lock enum
     */
    public void lock(String lockKey, RLockEnum... rLockEnum) {
        RLock rLock = getLock(lockKey, rLockEnum);
        rLock.lock();
    }

    /**
     * 释放锁
     *
     * @param lockKey   key
     * @param rLockEnum the r lock enum
     */
    public void unlock(String lockKey, RLockEnum... rLockEnum) {
        RLock rLock = getLock(lockKey, rLockEnum);
        // 是否有锁 && 是否当前线程
        if (rLock != null && rLock.isLocked() && rLock.isHeldByCurrentThread()) {
            rLock.unlock();
        }
    }

    /**
     * 加锁并设置有效期
     *
     * @param lockKey   key
     * @param timeout   有效时间，默认时间单位在实现类传入
     * @param rLockEnum the r lock enum
     */
    public void lock(String lockKey, int timeout, RLockEnum... rLockEnum) {
        RLock rLock = getLock(lockKey, rLockEnum);
        rLock.lock(timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * 加锁并设置有效期指定时间单位
     *
     * @param lockKey   key
     * @param timeout   有效时间
     * @param unit      时间单位
     * @param rLockEnum the r lock enum
     */
    public void lock(String lockKey, int timeout, TimeUnit unit, RLockEnum... rLockEnum) {
        RLock rLock = getLock(lockKey, rLockEnum);
        rLock.lock(timeout, unit);
    }

    /**
     * 尝试获取锁，获取到则持有该锁返回true,未获取到立即返回false
     *
     * @param lockKey   the lock key
     * @param rLockEnum the r lock enum
     * @return true -获取锁成功 false-获取锁失败
     */
    public boolean tryLock(String lockKey, RLockEnum... rLockEnum) {
        RLock rLock = getLock(lockKey, rLockEnum);
        return rLock.tryLock();
    }

    /**
     * 尝试获取锁，获取到则持有该锁leaseTime时间.
     * 若未获取到，在waitTime时间内一直尝试获取，超过watiTime还未获取到则返回false
     *
     * @param lockKey   key
     * @param waitTime  尝试获取时间
     * @param leaseTime 锁持有时间
     * @param unit      时间单位
     * @param rLockEnum the r lock enum
     * @return true -获取锁成功 false-获取锁失败
     * @throws InterruptedException the interrupted exception
     */
    public boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit, RLockEnum... rLockEnum) throws InterruptedException {
        RLock rLock = getLock(lockKey, rLockEnum);
        return rLock.tryLock(waitTime, leaseTime, unit);
    }

    /**
     * 锁是否被任意一个线程锁持有
     *
     * @param lockKey   the lock key
     * @param rLockEnum the r lock enum
     * @return true -被锁 false-未被锁
     */
    public boolean isLocked(String lockKey, RLockEnum... rLockEnum) {
        RLock rLock = getLock(lockKey, rLockEnum);
        return rLock.isLocked();
    }

}
