document.addEventListener("DOMContentLoaded", function () {
    const container = document.querySelector(".container");
    const registerBtn = document.querySelector(".register-btn");
    const loginBtn = document.querySelector(".login-btn");

    if (container && registerBtn && loginBtn) {
        registerBtn.addEventListener("click", () => container.classList.add("active"));
        loginBtn.addEventListener("click", () => container.classList.remove("active"));
    }

    (async function() {

        document.getElementById("loginForm")?.addEventListener("submit", async function (e) {
            e.preventDefault();
            await handleLogin();
        });
    })();


    async function handleLogin() {
        const email = document.getElementById("login-email").value;
        const password = document.getElementById("login-password").value;
        const userType = document.querySelector("input[name='userType']:checked")?.value || "KHACHHANG";

        try {
            Swal.fire({ title: "Đang xử lý...", allowOutsideClick: false, didOpen: () => Swal.showLoading() });

            const loginUrl = userType === "NHANVIEN" ? "/auth/nhanvien/login" : "/auth/khachhang/login";

            const response = await fetch(`http://localhost:8080${loginUrl}`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email, password, loaiTaiKhoan: userType }),
                credentials: "include"
            });

            const result = await response.json();

            if (response.ok) {
                Swal.fire({ icon: "success", title: "Đăng nhập thành công!", showConfirmButton: false, timer: 1000 })
                    .then(() => window.location.href = result.redirect || "/");
            } else {
                Swal.fire({ icon: "error", title: "Đăng nhập thất bại", text: result.message || "Sai email hoặc mật khẩu", confirmButtonText: "Thử lại" });
            }
        } catch (error) {
            console.error("Lỗi đăng nhập:", error);
            Swal.fire({ icon: "error", title: "Lỗi hệ thống", text: "Vui lòng thử lại sau", confirmButtonText: "OK" });
        }
    }

document.getElementById("registerForm").addEventListener("submit", async function (event) {
    event.preventDefault();

    // Lấy dữ liệu từ form
    const formData = {
        hoTen: document.getElementById("register-name").value.trim(),
        email: document.getElementById("register-email").value.trim(),
        password: document.getElementById("register-password").value,
        confirmPassword: document.getElementById("register-confirm-password").value,
        userType: "KHACHHANG"
    };

    // Kiểm tra các trường bắt buộc
    if (!formData.hoTen || !formData.email || !formData.password || !formData.confirmPassword) {
        Swal.fire({
            title: "Lỗi!",
            text: "Tất cả các trường đều là bắt buộc.",
            icon: "error",
            confirmButtonText: "OK"
        });
        return;
    }

    // Kiểm tra định dạng email
    const emailPattern = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}$/;
    if (!emailPattern.test(formData.email)) {
        Swal.fire({
            title: "Lỗi!",
            text: "Email không hợp lệ.",
            icon: "error",
            confirmButtonText: "OK"
        });
        return;
    }

    // Kiểm tra mật khẩu
    if (formData.password.length < 6) {
        Swal.fire({
            title: "Lỗi!",
            text: "Mật khẩu phải có ít nhất 6 ký tự.",
            icon: "error",
            confirmButtonText: "OK"
        });
        return;
    }

    // Kiểm tra mật khẩu xác nhận
    if (formData.password !== formData.confirmPassword) {
        Swal.fire({
            title: "Lỗi!",
            text: "Mật khẩu và xác nhận mật khẩu không khớp.",
            icon: "error",
            confirmButtonText: "OK"
        });
        return;
    }

    const registerUrl = formData.userType === "NHANVIEN"
        ? "/auth/nhanvien/register"
        : "/auth/khachhang/register";

    try {
        const response = await fetch(`http://localhost:8080${registerUrl}`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(formData),
            credentials: 'include'
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || "Đăng ký thất bại");
        }

        const result = await response.json();
       // Đăng ký
       Swal.fire({
           title: "Đăng ký thành công!",
           icon: "success",
           text: "Vui lòng đăng nhập.",
           confirmButtonText: "OK"
       }).then(() => location.reload());
    } catch (error) {
        console.error("Lỗi đăng ký:", error);
        Swal.fire({
            title: "Lỗi đăng ký!",
            text: error.message || "Vui lòng thử lại.",
            icon: "error",
            confirmButtonText: "OK"
        });
    }
});


    // Xử lý đăng xuất
    document.getElementById("logoutBtn")?.addEventListener("click", async function() {
        try {
            const response = await fetch("http://localhost:8080/auth/logout", {
                method: "POST",
                credentials: 'include'
            });

            if (response.ok) {
                window.location.href = "/auth/trendsetter";
            }
        } catch (error) {
            console.error("Lỗi đăng xuất:", error);
        }
    });
});