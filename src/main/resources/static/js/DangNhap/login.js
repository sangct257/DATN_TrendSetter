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

        const formData = {
            hoTen: document.getElementById("register-name").value,
            email: document.getElementById("register-email").value,
            password: document.getElementById("register-password").value,
            confirmPassword: document.getElementById("register-confirm-password").value,
            userType: document.querySelector("input[name='registerUserType']:checked").value
        };

        // Validate password match
        if (formData.password !== formData.confirmPassword) {
            alert("Mật khẩu không khớp!");
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
            alert("Đăng ký thành công! Vui lòng đăng nhập.");

            // Tự động chuyển về form đăng nhập
            container.classList.remove("active");
        } catch (error) {
            console.error("Lỗi đăng ký:", error);
            alert(error.message || "Lỗi đăng ký. Vui lòng thử lại.");
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