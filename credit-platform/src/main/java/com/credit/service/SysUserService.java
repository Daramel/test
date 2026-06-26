package com.credit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.credit.entity.SysUser;

/**
 * 系统用户Service接口
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * 根据用户名查询用户
     */
    SysUser findByUsername(String username);

    /**
     * 注册用户
     */
    SysUser register(String username, String password, String realName, String phone, String email);

    /**
     * 修改密码
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);

}
