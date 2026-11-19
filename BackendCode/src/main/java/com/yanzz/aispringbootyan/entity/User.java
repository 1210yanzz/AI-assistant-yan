package com.yanzz.aispringbootyan.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yanzz.aispringbootyan.Enum.UserStatus;
import com.yanzz.aispringbootyan.Enum.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/*import com.yanzz.aispringbootyan.enums.UserStatus;
import com.yanzz.aispringbootyan.enums.UserType;*/

import java.time.LocalDate;
import java.time.LocalDateTime;

// 用户实体类，对应数据库 user 表，存系统里所有用户（普通用户+管理员）的信息
// @Data 是 Lombok 注解：编译期自动帮我们生成 getter/setter、toString、equals、hashCode 方法，省得手写一大堆样板代码
// @Builder 是 Lombok 注解：自动生成建造者模式代码，可以用 User.builder().username("xxx").password("yyy").build() 这种链式调用创建对象
// @NoArgsConstructor 是 Lombok 注解：自动生成无参构造方法，MyBatis 用反射创建对象时必须要有无参构造
// @AllArgsConstructor 是 Lombok 注解：自动生成包含所有字段的构造方法
// @TableName 是 MyBatis-Plus（简称 MP）注解：括号里写表名，告诉 MP 这个类对应数据库里的哪张表，这里对应 user 表
@Data
@TableName("user")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    // 这是用户ID，主键字段
    // Long 类型比 Integer 能存更大的数，适合当主键，因为数据多了 Integer 可能不够用
    // @TableId 是 MP 注解：标记这个字段是数据库主键
    // type = IdType.AUTO 表示主键自增策略，也就是插入数据时不用自己设ID，数据库会自动生成递增的数字
    @TableId(type = IdType.AUTO)
    private Long id;

    // 登录用户名，全局唯一，注册后一般不能改
    // String 类型用来存文本内容
    // @NotBlank 是参数校验注解：不能为 null、不能是空字符串、也不能是纯空格，否则校验失败
    // @Size 是参数校验注解：限制字符串长度，这里要求用户名长度在 3 到 50 个字符之间
    // @Pattern 是参数校验注解：用正则表达式限制格式，这里只允许字母、数字和下划线
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3到50个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    // 邮箱地址，也可以当登录凭证用
    // @Email 是参数校验注解：自动校验字符串是不是符合邮箱格式（xxx@xx.xx）
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;

    // 手机号，选填项
    // 正则 ^1[3-9]\d{9}$ 意思是：1开头，第二位是3-9，后面跟9个数字，总共11位，正好是国内手机号格式
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    // 密码，存的是加密后的密文，绝对不能存明文！
    // 长度上限设255是为了容纳哈希加密后的长字符串
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 255, message = "密码长度必须在6到255个字符之间")
    private String password;

    // 昵称，展示给别人看的名字，可以重复，可以随便改
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;

    // 头像，存的是图片的访问路径或URL
    @Size(max = 255, message = "头像路径长度不能超过255个字符")
    private String avatar;

    // 性别：0-未知，1-男，2-女
    // Integer 类型存整数，适合这种状态码、类型码的场景
    private Integer gender;

    // 生日，LocalDate 是 Java 8 新的日期类型，只包含年月日，没有时分秒
    private LocalDate birthday;

    // 用户类型：1-普通用户，2-管理员，决定了这个人有什么权限
    // @TableField 是 MP 注解：当 Java 字段名和数据库列名不一致时，用它来显式指定对应关系
    // 这里 Java 字段叫 userType（驼峰命名），数据库列叫 user_type（下划线命名），MP 其实能自动转换，但写上更清晰
    @TableField("user_type")
    private Integer userType;

    // 账号状态：0-禁用，1-正常，被禁用的账号不能登录
    private Integer status;

    // 创建时间，LocalDateTime 包含年月日时分秒，适合记录时间戳
    @TableField("created_at")
    private LocalDateTime createdAt;

    // 更新时间，每次修改用户信息时更新这个字段
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    // 判断账号是否处于正常可用状态
    // 返回 true 表示可以正常使用，false 表示被禁用了
    public boolean isActive() {
        return UserStatus.NORMAL.getCode().equals(this.status);
    }

    // 获取用户的展示名称
    // 优先用昵称，如果昵称为空就退回用用户名，这样前端显示就不用每次都判断了
    public String getDisplayName() {
        return nickname != null && !nickname.trim().isEmpty() ? nickname : username;
    }

    // 把 userType 的数字码翻译成中文描述
    // 用枚举 UserType 的 fromCode 方法查找，找不到就返回"未知"
    public String getUserTypeDisplayName() {
        try {
            return UserType.fromCode(userType).getDescription();
        } catch (IllegalArgumentException e) {
            return "未知";
        }
    }

    // 把 status 的数字码翻译成中文描述
    public String getStatusDisplayName() {
        try {
            return UserStatus.fromCode(status).getDescription();
        } catch (IllegalArgumentException e) {
            return "未知";
        }
    }
}