package com.multimarket.services.impl;

import com.multimarket.dto.*;
import com.multimarket.models.*;
import com.multimarket.repositories.*;
import com.multimarket.services.Interfaces.AuthService;
import com.multimarket.utils.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PerfilRepository perfilRepository;
    private final RecuperacionPasswordRepository recuperacionRepository;
    private final VerificacionCorreoRepository verificacionRepository;
    private final SesionUsuarioRepository sesionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;

    public AuthServiceImpl(UsuarioRepository usuarioRepository, RolRepository rolRepository,
                           PerfilRepository perfilRepository, RecuperacionPasswordRepository recuperacionRepository,
                           VerificacionCorreoRepository verificacionRepository, SesionUsuarioRepository sesionRepository,
                           PasswordEncoder passwordEncoder, JwtUtils jwtUtils, UserDetailsService userDetailsService,
                           AuthenticationManager authenticationManager) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.perfilRepository = perfilRepository;
        this.recuperacionRepository = recuperacionRepository;
        this.verificacionRepository = verificacionRepository;
        this.sesionRepository = sesionRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
        this.authenticationManager = authenticationManager;
    }

    @Override
    @Transactional
    public String register(RegisterRequest request) {
        if (usuarioRepository.existsByCorreo(request.getCorreo())) {
            throw new IllegalArgumentException("El correo ya está registrado");
        }
        if (perfilRepository.existsByDni(request.getDni())) {
            throw new IllegalArgumentException("El DNI ya está registrado");
        }

        // Crear usuario
        Usuario usuario = new Usuario();
        usuario.setCorreo(request.getCorreo());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setEstado(true);
        usuario.setCorreoVerificado(false);
        usuario.setIntentosFallidos(0);
        usuario.setBloqueado(false);

        // Resolver Roles
        Set<Rol> roles = new HashSet<>();
        for (RolNombre rolNombre : request.getRoles()) {
            Rol rol = rolRepository.findByNombre(rolNombre)
                    .orElseGet(() -> rolRepository.save(new Rol(rolNombre, "Rol " + rolNombre.name())));
            roles.add(rol);
        }
        usuario.setRoles(roles);

        // Crear Perfil
        Perfil perfil = new Perfil();
        perfil.setNombres(request.getNombres());
        perfil.setApellidos(request.getApellidos());
        perfil.setDni(request.getDni());
        perfil.setTelefono(request.getTelefono());
        perfil.setDireccion(request.getDireccion());
        perfil.setFechaNacimiento(request.getFechaNacimiento());

        // Relación bidireccional
        usuario.setPerfil(perfil);

        // Guardar
        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        // Generar token de verificación
        VerificacionCorreo verificacion = new VerificacionCorreo();
        verificacion.setToken(UUID.randomUUID());
        verificacion.setFechaCreacion(LocalDateTime.now());
        verificacion.setFechaExpiracion(LocalDateTime.now().plusHours(24));
        verificacion.setVerificado(false);
        verificacion.setUsuario(usuarioGuardado);
        verificacionRepository.save(verificacion);

        // Simular envío de correo
        System.out.println("====== [SIMULACIÓN CORREO VERIFICACIÓN] ======");
        System.out.println("Para: " + usuarioGuardado.getCorreo());
        System.out.println("Token: " + verificacion.getToken());
        System.out.println("URL: http://localhost:8080/auth/verify?token=" + verificacion.getToken());
        System.out.println("===============================================");

        return "Usuario registrado con éxito. Se ha enviado un correo de verificación.";
    }

    @Override
    @Transactional
    public AdminUserResponse createAdminUser(AdminCreateUserRequest request) {
        if (usuarioRepository.existsByCorreo(request.getCorreo())) {
            throw new IllegalArgumentException("El correo ya está registrado");
        }

        if (request.getRoles() == null || request.getRoles().isEmpty()) {
            throw new IllegalArgumentException("Debe asignar al menos un rol");
        }

        Usuario usuario = new Usuario();
        usuario.setCorreo(request.getCorreo());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setEstado(request.getEstado() == null || request.getEstado());
        usuario.setCorreoVerificado(true);
        usuario.setIntentosFallidos(0);
        usuario.setBloqueado(false);

        Set<Rol> roles = new HashSet<>();
        for (RolNombre rolNombre : request.getRoles()) {
            Rol rol = rolRepository.findByNombre(rolNombre)
                    .orElseGet(() -> rolRepository.save(new Rol(rolNombre, "Rol " + rolNombre.name())));
            roles.add(rol);
        }
        usuario.setRoles(roles);

        Perfil perfil = new Perfil();
        String correo = request.getCorreo();
        String localPart = correo.contains("@") ? correo.substring(0, correo.indexOf('@')) : correo;
        String nombres = request.getNombres();
        String apellidos = request.getApellidos();
        String dni = request.getDni();

        perfil.setNombres(isBlank(nombres) ? buildDisplayName(localPart) : nombres.trim());
        perfil.setApellidos(isBlank(apellidos) ? buildProfileSuffix(request.getRoles()) : apellidos.trim());
        perfil.setDni(isBlank(dni) ? buildGeneratedDni() : dni.trim());
        perfil.setTelefono(request.getTelefono());
        perfil.setDireccion(request.getDireccion());
        perfil.setFechaNacimiento(java.time.LocalDate.of(1990, 1, 1));
        usuario.setPerfil(perfil);

        Usuario saved = usuarioRepository.save(usuario);
        return mapToAdminUserResponse(saved);
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request, String ip, String browser) {
        Usuario usuario = usuarioRepository.findByCorreo(request.getCorreo())
                .orElseThrow(() -> new IllegalArgumentException("Credenciales incorrectas"));

        if (usuario.getBloqueado()) {
            throw new IllegalStateException("Tu cuenta está bloqueada por demasiados intentos fallidos. Solicita recuperar contraseña.");
        }

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            int intentos = usuario.getIntentosFallidos() + 1;
            usuario.setIntentosFallidos(intentos);
            if (intentos >= 5) {
                usuario.setBloqueado(true);
                usuarioRepository.save(usuario);
                throw new IllegalStateException("Tu cuenta ha sido bloqueada por alcanzar 5 intentos fallidos.");
            }
            usuarioRepository.save(usuario);
            throw new IllegalArgumentException("Credenciales incorrectas. Intentos fallidos: " + intentos + "/5");
        }

        // Login Exitoso: Resetear intentos fallidos
        usuario.setIntentosFallidos(0);
        usuario.setUltimoAcceso(LocalDateTime.now());
        usuarioRepository.save(usuario);

        // Desactivar sesiones previas del mismo usuario
        List<SesionUsuario> sesionesPrevias = sesionRepository.findByUsuarioIdAndActiva(usuario.getId(), true);
        for (SesionUsuario sesionPrevia : sesionesPrevias) {
            sesionPrevia.setActiva(false);
            sesionPrevia.setFechaFin(LocalDateTime.now());
            sesionRepository.save(sesionPrevia);
        }

        // Crear nueva sesión de auditoría
        SesionUsuario nuevaSesion = new SesionUsuario();
        nuevaSesion.setIp(ip);
        nuevaSesion.setNavegador(browser);
        nuevaSesion.setFechaInicio(LocalDateTime.now());
        nuevaSesion.setActiva(true);
        nuevaSesion.setUsuario(usuario);
        sesionRepository.save(nuevaSesion);

        // Generar JWT
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getCorreo(), request.getPassword())
        );
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getCorreo());
        String jwtToken = jwtUtils.generateToken(userDetails);

        Set<String> rolesString = usuario.getRoles().stream()
                .map(rol -> rol.getNombre().name())
                .collect(Collectors.toSet());

        return new LoginResponse(jwtToken, usuario.getCorreo(), rolesString);
    }

    @Override
    @Transactional
    public String forgotPassword(ForgotPasswordRequest request) {
        Usuario usuario = usuarioRepository.findByCorreo(request.getCorreo())
                .orElseThrow(() -> new IllegalArgumentException("Si el correo está registrado, se enviará un enlace de recuperación."));

        // Generar token de recuperación
        RecuperacionPassword recuperacion = new RecuperacionPassword();
        recuperacion.setToken(UUID.randomUUID());
        recuperacion.setFechaCreacion(LocalDateTime.now());
        recuperacion.setFechaExpiracion(LocalDateTime.now().plusHours(1)); // 1 hora de validez
        recuperacion.setUtilizado(false);
        recuperacion.setUsuario(usuario);
        recuperacionRepository.save(recuperacion);

        // Simular envío de correo
        System.out.println("====== [SIMULACIÓN CORREO RECUPERACIÓN CONTRASEÑA] ======");
        System.out.println("Para: " + usuario.getCorreo());
        System.out.println("Token: " + recuperacion.getToken());
        System.out.println("URL: http://localhost:8080/auth/reset-password?token=" + recuperacion.getToken());
        System.out.println("=========================================================");

        return "Se ha enviado un correo con instrucciones para restablecer tu contraseña.";
    }

    @Override
    @Transactional
    public String resetPassword(ResetPasswordRequest request) {
        RecuperacionPassword recuperacion = recuperacionRepository.findByToken(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Token de recuperación inválido o inexistente"));

        if (recuperacion.getUtilizado()) {
            throw new IllegalStateException("El token ya ha sido utilizado");
        }

        if (recuperacion.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("El token ha expirado");
        }

        // Restablecer contraseña y desbloquear usuario
        Usuario usuario = recuperacion.getUsuario();
        usuario.setPassword(passwordEncoder.encode(request.getNewPassword()));
        usuario.setBloqueado(false);
        usuario.setIntentosFallidos(0);
        usuarioRepository.save(usuario);

        // Marcar token como utilizado
        recuperacion.setUtilizado(true);
        recuperacionRepository.save(recuperacion);

        return "Contraseña restablecida con éxito. Tu cuenta ha sido desbloqueada.";
    }

    @Override
    @Transactional
    public String changePassword(String correo, ChangePasswordRequest request) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.getOldPassword(), usuario.getPassword())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta");
        }

        usuario.setPassword(passwordEncoder.encode(request.getNewPassword()));
        usuarioRepository.save(usuario);

        return "Contraseña cambiada con éxito.";
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Perfil perfil = usuario.getPerfil();

        Set<String> rolesString = usuario.getRoles().stream()
                .map(rol -> rol.getNombre().name())
                .collect(Collectors.toSet());

        return new UserProfileResponse(
                usuario.getId(),
                usuario.getCorreo(),
                perfil.getNombres(),
                perfil.getApellidos(),
                perfil.getDni(),
                perfil.getTelefono(),
                perfil.getDireccion(),
                perfil.getFotoPerfil(),
                perfil.getFechaNacimiento(),
                rolesString
        );
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(String correo, UpdateProfileRequest request) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Perfil perfil = usuario.getPerfil();
        if (perfil == null) {
            perfil = new Perfil();
            usuario.setPerfil(perfil);
        }

        String nombrePersonal = request.getNombrePersonal();
        if (nombrePersonal != null && !nombrePersonal.trim().isEmpty()) {
            String[] parts = nombrePersonal.trim().split("\\s+", 2);
            perfil.setNombres(parts[0]);
            perfil.setApellidos(parts.length > 1 ? parts[1] : perfil.getApellidos());
        }

        if (request.getTelefono() != null) {
            perfil.setTelefono(request.getTelefono());
        }
        if (request.getDireccion() != null) {
            perfil.setDireccion(request.getDireccion());
        }
        if (request.getFotoPerfil() != null) {
            perfil.setFotoPerfil(request.getFotoPerfil());
        }

        usuarioRepository.save(usuario);
        return getProfile(correo);
    }

    private AdminUserResponse mapToAdminUserResponse(Usuario usuario) {
        Perfil perfil = usuario.getPerfil();
        return new AdminUserResponse(
                usuario.getId(),
                usuario.getCorreo(),
                usuario.getRoles().stream().map(rol -> rol.getNombre().name()).collect(Collectors.toList()),
                usuario.getEstado(),
                usuario.getCorreoVerificado(),
                usuario.getFechaRegistro(),
                usuario.getIntentosFallidos(),
                usuario.getBloqueado(),
                perfil != null ? perfil.getNombres() : null,
                perfil != null ? perfil.getApellidos() : null,
                perfil != null ? perfil.getDni() : null,
                perfil != null ? perfil.getTelefono() : null,
                perfil != null ? perfil.getDireccion() : null,
                perfil != null ? perfil.getFotoPerfil() : null,
                perfil != null ? perfil.getFechaNacimiento() : null
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String buildDisplayName(String value) {
        String cleaned = value == null ? "usuario" : value.replaceAll("[^a-zA-Z0-9]+", " ").trim();
        if (cleaned.isEmpty()) {
            return "Usuario";
        }
        String[] parts = cleaned.split("\\s+");
        String first = parts[0].substring(0, 1).toUpperCase() + parts[0].substring(1).toLowerCase();
        return first;
    }

    private String buildProfileSuffix(Set<RolNombre> roles) {
        if (roles.contains(RolNombre.ADMIN)) {
            return "Administrador";
        }
        if (roles.contains(RolNombre.VENDEDOR)) {
            return "Vendedor";
        }
        if (roles.contains(RolNombre.COMPRADOR)) {
            return "Comprador";
        }
        return "Usuario";
    }

    private String buildGeneratedDni() {
        return "AUTO" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
}
