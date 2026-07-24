package com.johanwork.warehouse.user.entity;

import com.johanwork.warehouse.common.audit.BaseEntity;
import com.johanwork.warehouse.merchant.entity.Merchant;
import com.johanwork.warehouse.role.entity.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
@NamedQueries({
        @NamedQuery(name = "User.getUserByRolesName",
        query = """
                SELECT u
                FROM User u
                JOIN FETCH u.roles r
                WHERE r.name = :roleName
                """),
        @NamedQuery(name = "User.getAllUserRoles",
                query = """
                SELECT u
                FROM User u
                JOIN FETCH u.roles r
                """)
})
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 20)
    @NotNull
    @Column(nullable = false, length = 20)
    private String name;

    @Size(max = 50)
    @NotNull
    @Column(nullable = false, length = 50, unique = true)
    private String email;

    @NotNull
    @Size(min = 6)
    @Column(nullable = false)
    private String password;

    private String photo;

    @Size(max = 15)
    @Column(length = 15, unique = true)
    private String phone;

    @ManyToMany
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @OneToOne(mappedBy = "keeper", fetch = FetchType.LAZY)
    private Merchant merchant;

}
