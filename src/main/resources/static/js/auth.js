document.addEventListener("DOMContentLoaded", function () {
    fetch("/auth")
        .then(response => response.text())
        .then(html => {
            document.getElementById("authContainer").innerHTML = html;
            attachEventListeners();
        })
        .catch(error => console.error("Lỗi tải auth.html:", error));
});

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

function attachEventListeners() {
    const logoutBtn = document.getElementById("logout-btn");
    if (logoutBtn) {
        logoutBtn.addEventListener("click", handleLogout);
    }

    document.getElementById("formLogin")?.addEventListener("submit", async function (e) {
        e.preventDefault();
        await handleLogin();
    });

    document.getElementById("formRegister")?.addEventListener("submit", async function (e) {
        e.preventDefault();
        await handleRegister();
    });
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
// Hàm kiểm tra email tồn tại
async function checkEmailExists(email) {
    try {
        const response = await fetch(`http://localhost:8080/auth/khachhang/check-email?email=${encodeURIComponent(email)}`);
        const result = await response.json();
        return response.ok && result.exists;
    } catch (error) {
        console.error("Lỗi khi kiểm tra email:", error);
        return false;
    }
}

// Hàm gọi API gửi email khôi phục mật khẩu
async function sendResetPasswordEmail() {
    const email = document.getElementById("forgot-email").value.trim();

    if (!email) {
        Swal.fire({
            icon: "error",
            title: "Lỗi",
            text: "Vui lòng nhập email",
            confirmButtonText: "OK"
        });
        return;
    }

    // Kiểm tra định dạng email
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
        Swal.fire({
            icon: "error",
            title: "Lỗi",
            text: "Email không hợp lệ",
            confirmButtonText: "OK"
        });
        return;
    }

    try {
        // Hiển thị thông báo khi đang xử lý
        Swal.fire({
            title: "Đang kiểm tra email...",
            allowOutsideClick: false,
            didOpen: () => Swal.showLoading()
        });

        // Kiểm tra email có tồn tại không
        const emailExists = await checkEmailExists(email);

        if (!emailExists) {
            Swal.fire({
                icon: "error",
                title: "Lỗi",
                text: "Email này chưa được đăng ký trong hệ thống",
                confirmButtonText: "OK"
            });
            return;
        }

        // Nếu email tồn tại, tiếp tục gửi email reset
        Swal.fire({
            title: "Đang gửi email...",
            allowOutsideClick: false,
            didOpen: () => Swal.showLoading()
        });

        const response = await fetch("http://localhost:8080/auth/forgot-password", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ email: email }),
        });

        const result = await response.json();

        if (response.ok) {
            Swal.fire({
                icon: "success",
                title: "Thành công!",
                text: "Email khôi phục mật khẩu đã được gửi. Vui lòng kiểm tra hộp thư của bạn.",
                confirmButtonText: "OK"
            }).then(() => {
                // Đóng modal quên mật khẩu
                $('#forgotPasswordModal').modal('hide');
            });
        } else {
            Swal.fire({
                icon: "error",
                title: "Lỗi",
                text: result.message || "Có lỗi khi gửi email. Vui lòng thử lại.",
                confirmButtonText: "OK"
            });
        }
    } catch (error) {
        console.error("Lỗi khi gửi email khôi phục:", error);
        Swal.fire({
            icon: "error",
            title: "Lỗi hệ thống",
            text: "Vui lòng thử lại sau.",
            confirmButtonText: "OK"
        });
    }
}

// Hàm kiểm tra token từ URL khi trang load
function checkForPasswordResetToken() {
    const urlParams = new URLSearchParams(window.location.search);
    const token = urlParams.get('token');

    if (token) {
        // Mở modal reset password
        $('#resetPasswordModal').modal('show');
        document.getElementById('reset-token').value = token;

        // Xóa token từ URL để tránh hiển thị lại
        window.history.replaceState({}, document.title, window.location.pathname);
    }
}

// Hàm xử lý reset password
async function submitNewPassword() {
    const token = document.getElementById('reset-token').value;
    const newPassword = document.getElementById('new-password').value;
    const confirmPassword = document.getElementById('confirm-password').value;

    if (!newPassword || !confirmPassword) {
        Swal.fire({
            icon: "error",
            title: "Lỗi",
            text: "Vui lòng nhập đầy đủ mật khẩu",
            confirmButtonText: "OK"
        });
        return;
    }

    if (newPassword.length < 6) {
        Swal.fire({
            icon: "error",
            title: "Lỗi",
            text: "Mật khẩu phải có ít nhất 6 ký tự",
            confirmButtonText: "OK"
        });
        return;
    }

    if (newPassword !== confirmPassword) {
        Swal.fire({
            icon: "error",
            title: "Lỗi",
            text: "Mật khẩu không khớp",
            confirmButtonText: "OK"
        });
        return;
    }

    try {
        Swal.fire({
            title: "Đang xử lý...",
            allowOutsideClick: false,
            didOpen: () => Swal.showLoading()
        });

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

        const result = await response.json();

        if (response.ok) {
            Swal.fire({
                icon: "success",
                title: "Thành công!",
                text: "Mật khẩu đã được đặt lại thành công.",
                confirmButtonText: "OK"
            }).then(() => {
                // Đóng modal reset password
                $('#resetPasswordModal').modal('hide');
                // Mở modal đăng nhập
                $('#authModal').modal('show');
                $('#login-tab').tab('show');
            });
        } else {
            Swal.fire({
                icon: "error",
                title: "Lỗi",
                text: result.message || "Có lỗi khi đặt lại mật khẩu.",
                confirmButtonText: "OK"
            });
        }
    } catch (error) {
        console.error("Lỗi khi đặt lại mật khẩu:", error);
        Swal.fire({
            icon: "error",
            title: "Lỗi hệ thống",
            text: "Vui lòng thử lại sau.",
            confirmButtonText: "OK"
        });
    }
}
document.addEventListener('DOMContentLoaded', function() {
    console.log("DOMContentLoaded triggered"); // Kiểm tra nếu sự kiện DOMContentLoaded chạy
    checkForPasswordResetToken();

    // Thêm event listener cho nút gửi email reset
    const sendResetEmailBtn = document.getElementById('sendResetEmailBtn');
    if (sendResetEmailBtn) {
        sendResetEmailBtn.addEventListener('click', function() {
            console.log("Gửi liên kết clicked"); // Kiểm tra khi sự kiện click xảy ra
            sendResetPasswordEmail();
        });
    }
    const submitNewPasswordBtn = document.getElementById('submitNewPasswordBtn');
    if (submitNewPasswordBtn) {
        submitNewPasswordBtn.addEventListener('click', function () {
            console.log("submit"); // Kiểm tra khi sự kiện click xảy ra
            submitNewPassword();
        });
    }
});
