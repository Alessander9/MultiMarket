package com.multimarket.services.Interfaces;

import com.multimarket.dto.*;

public interface AuthService {
    String register(RegisterRequest request);
    LoginResponse login(LoginRequest request, String ip, String browser);
    String forgotPassword(ForgotPasswordRequest request);
    String resetPassword(ResetPasswordRequest request);
    String changePassword(String correo, ChangePasswordRequest request);
    UserProfileResponse getProfile(String correo);
}
