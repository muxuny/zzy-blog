package com.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.entity.User;
import com.blog.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public PageResult<User> list(@RequestParam(defaultValue = "1") long page,
                                  @RequestParam(defaultValue = "10") long size) {
        IPage<User> result = userService.page(new Page<>(page, size),
                new LambdaQueryWrapper<User>().orderByDesc(User::getCreatedAt));
        result.getRecords().forEach(u -> u.setPassword(null));
        return PageResult.success(result);
    }

    @GetMapping("/{id}")
    public Result<User> detail(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user != null) user.setPassword(null);
        return Result.success(user);
    }

    @PutMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id) {
        userService.approveUser(id);
        return Result.success();
    }

    @PutMapping("/{id}/disable")
    public Result<Void> disable(@PathVariable Long id) {
        userService.disableUser(id);
        return Result.success();
    }
}
