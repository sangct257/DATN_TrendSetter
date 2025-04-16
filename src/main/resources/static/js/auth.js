document.addEventListener("DOMContentLoaded", function() {
    initAllEventListeners();

    handlePasswordResetFlow();
});


function initAllEventListeners() {
    // Auth
    document.getElementById("formLogin")?.addEventListener("submit", handleLogin);
    document.getElementById("formRegister")?.addEventListener("submit", handleRegister);
    document.getElementById("logout-btn")?.addEventListener("click", handleLogout);

    // Password Reset
    document.getElementById("sendResetEmailBtn")?.addEventListener("click", handleSendResetEmail);
    document.getElementById("submitNewPasswordBtn")?.addEventListener("click", handleResetPassword);
}

function handlePasswordResetFlow() {
    const urlParams = new URLSearchParams(window.location.search);
    const token = urlParams.get('token');

    if (token) {
        document.getElementById("reset-token").value = token;
        $('#resetPasswordModal').modal('show');


        showInfo("Vui lòng nhập mật khẩu mới");
    }
}


async function handleLogin(e) {
    e.preventDefault();
    const email = document.getElementById("login-email").value;
    const password = document.getElementById("login-password").value;

    try {
        showLoading("Đang đăng nhập...");
        const response = await fetch("/auth/khachhang/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, password, loaiTaiKhoan: "KHACHHANG" }),
            credentials: "include"
        });

        const result = await response.json();
        if (!response.ok) throw new Error(result.message || "Đăng nhập thất bại");

        showSuccess("Đăng nhập thành công!").then(() => {
            window.location.href = result.redirect || "/";
        });
    } catch (error) {
        showError(error.message);
    }
}

async function handleLogout() {
    try {
        showLoading("Đang đăng xuất...");
        const response = await fetch("/auth/logout", {
            method: "POST",
            credentials: "include"
        });
        if (!response.ok) throw new Error("Đăng xuất không thành công");
        window.location.href = "/login";
    } catch (error) {
        showError(error.message);
    }
}


async function handleSendResetEmail() {
    const email = document.getElementById("forgot-email").value.trim();

    if (!validateEmail(email)) {
        showError("Email không hợp lệ");
        return;
    }

    try {
        showLoading("Đang gửi email...");
        const response = await fetch("/auth/forgot-password", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email })
        });

        const result = await response.json();
        if (!response.ok) throw new Error(result.message || "Gửi email thất bại");

        showSuccess("Đã gửi link reset password đến email của bạn!").then(() => {
            $('#forgotPasswordModal').modal('hide');
        });
    } catch (error) {
        showError(error.message);
    }
}

async function handleResetPassword() {
    const token = document.getElementById("reset-token").value;
    const newPassword = document.getElementById("new-password").value;
    const confirmPassword = document.getElementById("confirm-password").value;

    // Validate
    if (!newPassword || !confirmPassword) {
        showError("Vui lòng nhập đầy đủ mật khẩu");
        return;
    }
    if (newPassword !== confirmPassword) {
        showError("Mật khẩu không khớp");
        return;
    }

    try {
        showLoading("Đang xử lý...");

        const response = await fetch("http://localhost:8080/auth/reset-password", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                token: token,
                newPassword: newPassword
            })
        });

        // Kiểm tra lỗi HTTP
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || "Lỗi khi đặt lại mật khẩu");
        }

        const result = await response.json();
        showSuccess(result.message).then(() => {
            window.location.href = "/trang-chu";
        });
    } catch (error) {
        showError(error.message);
        console.error("Chi tiết lỗi:", error); // Log lỗi ra console
    }
}


function validateEmail(email) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

function showLoading(message) {
    Swal.fire({
        title: message,
        allowOutsideClick: false,
        didOpen: () => Swal.showLoading()
    });
}

function showSuccess(message) {
    return Swal.fire({
        icon: "success",
        title: "Thành công!",
        text: message,
        confirmButtonText: "OK"
    });
}

function showError(message) {
    return Swal.fire({
        icon: "error",
        title: "Lỗi",
        text: message,
        confirmButtonText: "OK"
    });
}

function showInfo(message) {
    return Swal.fire({
        icon: "info",
        title: "Thông tin",
        text: message,
        confirmButtonText: "OK"
    });
}
// Modal helper function
function openAuthModal(authType = "login") {
    if (!document.getElementById("authModal")) {
        setTimeout(() => openAuthModal(authType), 100);
        return;
    }

    if (authType === "register") {
        document.querySelector("#register-tab").click();
    } else {
        document.querySelector("#login-tab").click();
    }

    new bootstrap.Modal(document.getElementById("authModal")).show();
}

async function handleLogin() {
    const email = document.getElementById("login-email").value;
    const password = document.getElementById("login-password").value;
    const userType = "KHACHHANG";

    try {
        Swal.fire({
            title: "Đang xử lý...",
            allowOutsideClick: false,
            didOpen: () => Swal.showLoading()
        });

        const response = await fetch("http://localhost:8080/auth/khachhang/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, password, loaiTaiKhoan: userType }),
            credentials: "include"
        });

        const result = await response.json();

        if (response.ok) {
            Swal.fire({
                icon: "success",
                title: "Đăng nhập thành công!",
                showConfirmButton: false,
                timer: 1000
            }).then(() => {
                window.location.href = result.redirect || "/";
            });
        } else {
            Swal.fire({
                icon: "error",
                title: "Đăng nhập thất bại",
                text: result.message || "Sai email hoặc mật khẩu",
                confirmButtonText: "Thử lại"
            });
        }
    } catch (error) {
        Swal.fire({
            icon: "error",
            title: "Lỗi hệ thống",
            text: "Vui lòng thử lại sau",
            confirmButtonText: "OK"
        });
    }
}

async function handleRegister() {
    const name = document.getElementById("register-name").value;
    const email = document.getElementById("register-email").value;
    const password = document.getElementById("register-password").value;

    try {
        Swal.fire({
            title: "Đang xử lý...",
            allowOutsideClick: false,
            didOpen: () => Swal.showLoading()
        });

        const response = await fetch("http://localhost:8080/auth/khachhang/register", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ hoTen: name, email, password }),
            credentials: "include"
        });

        const result = await response.json();

        if (response.ok) {
            Swal.fire({
                icon: "success",
                title: "Đăng ký thành công!",
                showConfirmButton: false,
                timer: 1000
            }).then(() => {
                document.querySelector("#login-tab").click();
            });
        } else {
            Swal.fire({
                icon: "error",
                title: "Đăng ký thất bại",
                text: result.message || "Vui lòng thử lại",
                confirmButtonText: "OK"
            });
        }
    } catch (error) {
        Swal.fire({
            icon: "error",
            title: "Lỗi hệ thống",
            text: "Vui lòng thử lại sau",
            confirmButtonText: "OK"
        });
    }
}

function openAuthModal(authType = "login") {
    if (!document.getElementById("authModal")) {
        console.error("Modal chưa tải xong. Chờ thêm 100ms...");
        setTimeout(() => openAuthModal(authType), 100);
        return;
    }

    if (authType === "register") {
        document.querySelector("#register-tab").click();
    } else {
        document.querySelector("#login-tab").click();
    }

    new bootstrap.Modal(document.getElementById("authModal")).show();
}
