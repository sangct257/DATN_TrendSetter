document.addEventListener("DOMContentLoaded", function () {
    fetch("/auth")
        .then(response => response.text())
        .then(html => {
            document.getElementById("authContainer").innerHTML = html;
            attachEventListeners();
        })
        .catch(error => console.error("Lỗi tải auth.html:", error));
});

// Hàm mở modal đăng nhập/đăng ký
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

// Gán sự kiện sau khi auth.html được load xong
function attachEventListeners() {
    document.getElementById("formLogin")?.addEventListener("submit", async function (e) {
        e.preventDefault();
        await handleLogin();
    });

    document.getElementById("formRegister")?.addEventListener("submit", async function (e) {
        e.preventDefault();
        await handleRegister();
    });
}

// Xử lý đăng nhập (chỉ khách hàng)
async function handleLogin() {
    const email = document.getElementById("login-email").value;
    const password = document.getElementById("login-password").value;
    const userType = "KHACHHANG";  // Mặc định là khách hàng

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

// Xử lý đăng ký (chỉ khách hàng)
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
                document.querySelector("#login-tab").click(); // Chuyển về form đăng nhập
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
