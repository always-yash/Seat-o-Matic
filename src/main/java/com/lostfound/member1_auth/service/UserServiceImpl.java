package com.lostfound.member1_auth.service;

import com.lostfound.member1_auth.dao.UserDAO;
import com.lostfound.member1_auth.dao.UserDAOImpl;
import com.lostfound.member1_auth.util.BCryptUtil;
import com.lostfound.member1_auth.util.ValidationUtil;
import com.lostfound.model.User;

import java.sql.SQLException;

/**
 * Implementation of UserService with robust business rules,
 * validations, and security checks.
 */
public class UserServiceImpl implements UserService {

    private final UserDAO userDAO;
    private final AuthOtpService authOtpService = AuthOtpService.getInstance();

    public UserServiceImpl() {
        this.userDAO = new UserDAOImpl();
    }

    public UserServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public User register(User user, String confirmPassword) throws Exception {
        if (user == null) {
            throw new IllegalArgumentException("User details cannot be empty.");
        }

        // Validate name
        if (!ValidationUtil.isNonEmpty(user.getName()) || user.getName().trim().length() < 2) {
            throw new IllegalArgumentException("Full Name is required and must be at least 2 characters.");
        }

        // Validate email
        if (!ValidationUtil.isValidEmail(user.getEmail())) {
            throw new IllegalArgumentException("Please provide a valid college email address.");
        }

        // Check duplicate email
        User existing = userDAO.findByEmail(user.getEmail());
        if (existing != null) {
            throw new IllegalArgumentException("An account with this email address already exists.");
        }

        // Validate password
        if (!ValidationUtil.isValidPassword(user.getPassword())) {
            throw new IllegalArgumentException("Password must be at least 8 characters long and contain both letters and numbers/symbols.");
        }

        // Validate confirm password
        if (confirmPassword == null || !user.getPassword().equals(confirmPassword)) {
            throw new IllegalArgumentException("Password confirmation does not match.");
        }

        // Validate phone
        if (!ValidationUtil.isValidPhone(user.getPhone())) {
            throw new IllegalArgumentException("Please enter a valid phone number (digits only, 7-15 chars).");
        }

        // Security rule: NEVER allow self-registration as ADMIN
        String requestedRole = user.getRole();
        if ("ADMIN".equalsIgnoreCase(requestedRole)) {
            user.setRole("STUDENT");
        } else if ("FACULTY".equalsIgnoreCase(requestedRole)) {
            user.setRole("FACULTY");
        } else {
            user.setRole("STUDENT");
        }

        // Set default status
        user.setStatus("ACTIVE");

        // Hash password securely with BCrypt
        String hashedPassword = BCryptUtil.hashPassword(user.getPassword());
        user.setPassword(hashedPassword);

        // Persist
        return userDAO.register(user);
    }

    @Override
    public User login(String email, String password) throws Exception {
        if (!ValidationUtil.isNonEmpty(email) || !ValidationUtil.isNonEmpty(password)) {
            throw new IllegalArgumentException("Email and password are required.");
        }

        User user = userDAO.findByEmail(email.trim());
        if (user == null) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        // Check account status
        if (!user.isActive()) {
            throw new IllegalStateException("Your account has been deactivated or blocked by an administrator.");
        }

        // Verify BCrypt password
        boolean matches = BCryptUtil.checkPassword(password, user.getPassword());
        if (!matches) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        return user;
    }

    @Override
    public User getProfile(Long userId) throws Exception {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required.");
        }
        User user = userDAO.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }
        return user;
    }

    @Override
    public boolean updateProfile(Long userId, String name, String phone, String department) throws Exception {
        User user = getProfile(userId);
        if (!ValidationUtil.isNonEmpty(name) || name.trim().length() < 2) {
            throw new IllegalArgumentException("Full Name must be at least 2 characters.");
        }
        if (!ValidationUtil.isValidPhone(phone)) {
            throw new IllegalArgumentException("Please enter a valid phone number.");
        }

        user.setName(name.trim());
        user.setPhone(phone != null ? phone.trim() : null);
        user.setDepartment(department != null ? department.trim() : null);

        return userDAO.updateProfile(user);
    }

    @Override
    public boolean changePassword(Long userId, String currentPassword, String newPassword, String confirmPassword) throws Exception {
        User user = getProfile(userId);

        if (!BCryptUtil.checkPassword(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }

        if (!ValidationUtil.isValidPassword(newPassword)) {
            throw new IllegalArgumentException("New password must be at least 8 characters long and contain both letters and numbers/symbols.");
        }

        if (confirmPassword == null || !newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("New password confirmation does not match.");
        }

        if (currentPassword.equals(newPassword)) {
            throw new IllegalArgumentException("New password cannot be the same as the current password.");
        }

        String hashedNewPassword = BCryptUtil.hashPassword(newPassword);
        return userDAO.updatePassword(userId, hashedNewPassword);
    }

    @Override
    public User loginOrRegisterWithGoogle(String email, String name, String pictureUrl) throws Exception {
        if (!ValidationUtil.isNonEmpty(email) || !ValidationUtil.isValidEmail(email)) {
            throw new IllegalArgumentException("A valid Google / Gmail address is required.");
        }

        String normalizedEmail = email.trim().toLowerCase();
        User existingUser = userDAO.findByEmail(normalizedEmail);

        if (existingUser != null) {
            if (!existingUser.isActive()) {
                throw new IllegalStateException("Your account has been deactivated or blocked by an administrator.");
            }
            return existingUser;
        }

        // Auto-register new account for Google / Gmail users
        User newUser = new User();
        String displayName = (name != null && !name.trim().isEmpty()) 
                ? name.trim() 
                : normalizedEmail.split("@")[0];

        newUser.setName(displayName);
        newUser.setEmail(normalizedEmail);
        // Generate random BCrypt password for OAuth accounts
        newUser.setPassword(BCryptUtil.hashPassword("GoogleOAuth$" + java.util.UUID.randomUUID().toString()));
        newUser.setRole("STUDENT");
        newUser.setDepartment("Campus Student (Google Auth)");
        newUser.setStatus("ACTIVE");
        newUser.setPhone("");

        return userDAO.register(newUser);
    }

    @Override
    public String sendLoginOtp(String email) throws Exception {
        if (!ValidationUtil.isNonEmpty(email) || !ValidationUtil.isValidEmail(email)) {
            throw new IllegalArgumentException("A valid email address is required to receive the login code.");
        }
        return authOtpService.generateAndSendOtp(email.trim().toLowerCase(), AuthOtpService.OtpPurpose.LOGIN, 5);
    }

    @Override
    public User loginWithOtp(String email, String otp) throws Exception {
        if (!ValidationUtil.isNonEmpty(email) || !ValidationUtil.isValidEmail(email)) {
            throw new IllegalArgumentException("A valid email address is required.");
        }
        if (!ValidationUtil.isNonEmpty(otp) || otp.trim().length() != 6) {
            throw new IllegalArgumentException("Please enter the 6-digit verification OTP code.");
        }

        String normalizedEmail = email.trim().toLowerCase();
        boolean verified = authOtpService.verifyOtp(normalizedEmail, AuthOtpService.OtpPurpose.LOGIN, otp.trim());
        if (!verified) {
            throw new IllegalArgumentException("Invalid or expired login OTP code. Please request a new code.");
        }

        User existingUser = userDAO.findByEmail(normalizedEmail);
        if (existingUser != null) {
            if (!existingUser.isActive()) {
                throw new IllegalStateException("Your account has been deactivated or blocked by an administrator.");
            }
            return existingUser;
        }

        // Auto-provision user account on successful OTP authentication
        User newUser = new User();
        String namePart = normalizedEmail.split("@")[0];
        newUser.setName(Character.toUpperCase(namePart.charAt(0)) + (namePart.length() > 1 ? namePart.substring(1) : ""));
        newUser.setEmail(normalizedEmail);
        newUser.setPassword(BCryptUtil.hashPassword("OTP_AUTH$" + java.util.UUID.randomUUID().toString()));
        newUser.setRole("STUDENT");
        newUser.setDepartment("Campus Student (OTP Auth)");
        newUser.setStatus("ACTIVE");
        newUser.setPhone("");

        return userDAO.register(newUser);
    }

    @Override
    public String sendForgotPasswordOtp(String email) throws Exception {
        if (!ValidationUtil.isNonEmpty(email) || !ValidationUtil.isValidEmail(email)) {
            throw new IllegalArgumentException("A valid registered email address is required.");
        }

        String normalizedEmail = email.trim().toLowerCase();
        User user = userDAO.findByEmail(normalizedEmail);
        if (user == null) {
            throw new IllegalArgumentException("No registered campus account found for " + normalizedEmail + ".");
        }
        if (!user.isActive()) {
            throw new IllegalStateException("This account is currently blocked or deactivated.");
        }

        return authOtpService.generateAndSendOtp(normalizedEmail, AuthOtpService.OtpPurpose.FORGOT_PASSWORD, 10);
    }

    @Override
    public boolean resetPasswordWithOtp(String email, String otp, String newPassword, String confirmPassword) throws Exception {
        if (!ValidationUtil.isNonEmpty(email) || !ValidationUtil.isValidEmail(email)) {
            throw new IllegalArgumentException("A valid email address is required.");
        }
        if (!ValidationUtil.isNonEmpty(otp) || otp.trim().length() != 6) {
            throw new IllegalArgumentException("Please enter the 6-digit password recovery code.");
        }
        if (!ValidationUtil.isValidPassword(newPassword)) {
            throw new IllegalArgumentException("New password must be at least 8 characters and contain letters and numbers/symbols.");
        }
        if (confirmPassword == null || !newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Password confirmation does not match the new password.");
        }

        String normalizedEmail = email.trim().toLowerCase();
        boolean verified = authOtpService.verifyOtp(normalizedEmail, AuthOtpService.OtpPurpose.FORGOT_PASSWORD, otp.trim());
        if (!verified) {
            throw new IllegalArgumentException("Invalid or expired password recovery code. Please request a new code.");
        }

        User user = userDAO.findByEmail(normalizedEmail);
        if (user == null) {
            throw new IllegalArgumentException("User account not found.");
        }

        String hashedNewPassword = BCryptUtil.hashPassword(newPassword);
        return userDAO.updatePassword(user.getUserId(), hashedNewPassword);
    }
}
