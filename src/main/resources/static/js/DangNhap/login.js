document.addEventListener("DOMContentLoaded", function () {
    const container = document.querySelector('.container');
    const registerBtn = document.querySelector('.register-btn');
    const loginBtn = document.querySelector('.login-btn');

    if (container && registerBtn && loginBtn) {
        registerBtn.addEventListener('click', () => container.classList.add('active'));
        loginBtn.addEventListener('click', () => container.classList.remove('active'));
    }

    // 🔹 Hàm lấy token từ localStorage và tự động thêm vào header Authorization
    const setAuthHeaders = () => {
        const token = localStorage.getItem("token");
        if (token) {
            return { "Authorization": `Bearer ${token}` };
        }
        return {};
    };

    // 🔹 Kiểm tra token khi tải trang (tránh mất session khi chuyển trang)
    const checkAuthOnLoad = () => {
        const token = localStorage.getItem("token");
        if (!token) {
            console.warn("⚠️ Không tìm thấy token, có thể sẽ bị chặn.");
        } else {
            console.log("✅ Token tồn tại, tiếp tục xác thực.");
        }
    };

    checkAuthOnLoad(); // Gọi hàm kiểm tra khi trang load

    // 🔹 Xử lý form login
    loginForm.addEventListener("submit", async function (event) {
        event.preventDefault();

        const email = document.getElementById("login-email").value;
        const password = document.getElementById("login-password").value;

        try {
            const response = await fetch("http://localhost:8080/auth/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email, password })
            });


            if (!response.ok) {
                console.error(`❌ API lỗi ${response.status}:`, response.statusText);
                alert("Đăng nhập thất bại!");
                return;
            }

            const result = await response.json();
            console.log("📥 Kết quả từ API:", result);

            if (result.token && result.roles && result.redirect) {
                localStorage.setItem("token", result.token);
                localStorage.setItem("roles", JSON.stringify(result.roles));
                window.location.href = result.redirect;
            } else {
                alert("Đăng nhập thất bại! Kiểm tra lại email/mật khẩu.");
            }
        } catch (error) {
            console.error("Lỗi đăng nhập:", error);
            alert("Lỗi kết nối đến server.");
        }
    });



    // 🔹 Xử lý form register
    const registerForm = document.getElementById("registerForm");
    if (registerForm) {
        registerForm.addEventListener("submit", async function (event) {
            event.preventDefault();

            const username = document.getElementById("register-username").value;
            const email = document.getElementById("register-email").value;
            const password = document.getElementById("register-password").value;
            const roleElement = document.getElementById("register-role");
            const role = roleElement ? roleElement.value : "";

            try {
                const response = await fetch("http://localhost:8080/auth/register", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ username, email, password, role })
                });

                const text = await response.text();
                console.log("📥 Response từ server:", text);

                if (!text) {
                    alert("Lỗi: Server không trả về dữ liệu.");
                    return;
                }

                let result;
                try {
                    result = JSON.parse(text);
                } catch (parseError) {
                    console.error("❌ Lỗi khi parse JSON:", parseError);
                    alert("Lỗi: Server không trả về JSON hợp lệ.");
                    return;
                }

                if (response.ok) {
                    alert("✅ Đăng ký thành công! Hãy đăng nhập.");
                    window.location.href = '/auth/login';
                } else {
                    alert("❌ Lỗi đăng ký: " + (result.message || "Không xác định"));
                }
            } catch (error) {
                console.error("❌ Lỗi khi đăng ký:", error);
                alert("Đã xảy ra lỗi kết nối đến server.");
            }
        });
    }

});
