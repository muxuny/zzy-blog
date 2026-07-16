package com.blog.common;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据库实体公共字段，集中承载审计、逻辑删除和乐观锁信息。
 */
@Data
public abstract class BaseEntity implements Serializable {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 创建人用户名，业务上也用于区分部分个人资源归属。 */
    @TableField(fill = FieldFill.INSERT)
    private String createdBy;

    /** 创建时间，由 MyBatis-Plus 自动填充。 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 最近更新人用户名。 */
    @TableField(fill = FieldFill.UPDATE)
    private String updatedBy;

    /** 最近更新时间，由 MyBatis-Plus 自动填充。 */
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updatedAt;

    /** 逻辑删除标记，避免物理删除历史数据。 */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    /** 乐观锁版本号，用于并发更新保护。 */
    @Version
    @TableField("version")
    private Integer version;
}
