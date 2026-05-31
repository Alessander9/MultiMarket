package com.multimarket.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "usuarios")
public class Usuario extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Email
    @Size(max = 100)
    @Column(nullable = false, unique = true, length = 100)
    private String correo;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String password;

    @NotNull
    @Column(nullable = false)
    private Boolean estado = true;

    @NotNull
    @Column(name = "correo_verificado", nullable = false)
    private Boolean correoVerificado = false;

    @NotNull
    @Column(name = "intentos_fallidos", nullable = false)
    private Integer intentosFallidos = 0;

    @NotNull
    @Column(nullable = false)
    private Boolean bloqueado = false;

    @NotNull
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    private Perfil perfil;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "usuario_rol",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    private Set<Rol> roles = new HashSet<>();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<RecuperacionPassword> recuperaciones = new ArrayList<>();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<VerificacionCorreo> verificaciones = new ArrayList<>();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<SesionUsuario> sesiones = new ArrayList<>();

    public Usuario() {}

    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();
        if (this.fechaRegistro == null) {
            this.fechaRegistro = LocalDateTime.now();
        }
        if (this.estado == null) {
            this.estado = true;
        }
        if (this.correoVerificado == null) {
            this.correoVerificado = false;
        }
        if (this.intentosFallidos == null) {
            this.intentosFallidos = 0;
        }
        if (this.bloqueado == null) {
            this.bloqueado = false;
        }
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }

    public Boolean getCorreoVerificado() {
        return correoVerificado;
    }

    public void setCorreoVerificado(Boolean correoVerificado) {
        this.correoVerificado = correoVerificado;
    }

    public Integer getIntentosFallidos() {
        return intentosFallidos;
    }

    public void setIntentosFallidos(Integer intentosFallidos) {
        this.intentosFallidos = intentosFallidos;
    }

    public Boolean getBloqueado() {
        return bloqueado;
    }

    public void setBloqueado(Boolean bloqueado) {
        this.bloqueado = bloqueado;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public LocalDateTime getUltimoAcceso() {
        return ultimoAcceso;
    }

    public void setUltimoAcceso(LocalDateTime ultimoAcceso) {
        this.ultimoAcceso = ultimoAcceso;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        if (perfil == null) {
            if (this.perfil != null) {
                this.perfil.setUsuario(null);
            }
        } else {
            perfil.setUsuario(this);
        }
        this.perfil = perfil;
    }

    public Set<Rol> getRoles() {
        return roles;
    }

    public void setRoles(Set<Rol> roles) {
        this.roles = roles;
    }

    public List<RecuperacionPassword> getRecuperaciones() {
        return recuperaciones;
    }

    public void setRecuperaciones(List<RecuperacionPassword> recuperaciones) {
        this.recuperaciones = recuperaciones;
    }

    public List<VerificacionCorreo> getVerificaciones() {
        return verificaciones;
    }

    public void setVerificaciones(List<VerificacionCorreo> verificaciones) {
        this.verificaciones = verificaciones;
    }

    public List<SesionUsuario> getSesiones() {
        return sesiones;
    }

    public void setSesiones(List<SesionUsuario> sesiones) {
        this.sesiones = sesiones;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return id != null && id.equals(usuario.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
