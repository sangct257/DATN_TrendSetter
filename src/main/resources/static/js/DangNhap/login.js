document.addEventListener("DOMContentLoaded", function () {
    const container = document.querySelector(".container");
    const registerBtn = document.querySelector(".register-btn");
    const loginBtn = document.querySelector(".login-btn");

    if (container && registerBtn && loginBtn) {
        registerBtn.addEventListener("click", () => container.classList.add("active"));
        loginBtn.addEventListener("click", () => container.classList.remove("active"));
    }

    const setAuthHeaders = () => {
        const token = localStorage.getItem("token");
        return token ? { "Authorization": `Bearer ${token}` } : {};
    };

    const checkAuthOnLoad = () => {
        const token = localStorage.getItem("token");
        if (!token) {
            console.warn("⚠️ Không tìm thấy token, có thể sẽ bị chặn.");
        } else {
            console.log("✅ Token tồn tại, tiếp tục xác thực.");
        }
    };

    checkAuthOnLoad();

    document.getElementById("loginForm").addEventListener("submit", async function (event) {
        event.preventDefault();

        const emailInput = document.getElementById("login-email");
        const passwordInput = document.getElementById("login-password");
        const userTypeInput = document.querySelector("input[name='userType']");

        if (!emailInput || !passwordInput || !userTypeInput) {
            console.error("Một hoặc nhiều phần tử không tồn tại trong DOM.");
            return;
        }

        const email = emailInput.value;
        const password = passwordInput.value;
        const userType = userTypeInput.value; 

        const loginUrl = userType === "NHANVIEN" ? "/auth/nhanvien/login" : "/auth/khachhang/login";

        try {
            const response = await fetch(`http://localhost:8080${loginUrl}`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email, password })
            });

            if (!response.ok) {
                alert("Đăng nhập thất bại! Vui lòng kiểm tra lại thông tin.");
                return;
            }

            const result = await response.json();
            localStorage.setItem("token", result.token);
            localStorage.setItem("roles", JSON.stringify(result.roles));
            window.location.href = result.redirect;
        } catch (error) {
            console.error("Lỗi đăng nhập:", error);
            alert("Lỗi kết nối đến server.");
        }
    });
});