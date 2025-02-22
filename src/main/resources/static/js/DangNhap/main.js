const container = document.querySelector('.container');
const registerBtn = document.querySelector('.register-btn');
const loginBtn = document.querySelector('.login-btn');

registerBtn.addEventListener('click', () => {
    container.classList.add('active');
})

loginBtn.addEventListener('click', () => {
    container.classList.remove('active');
})
document.addEventListener("DOMContentLoaded", function () {
    // Xử lý đăng nhập
    document.getElementById("loginForm").addEventListener("submit", async function (event) {
        event.preventDefault();

        const username = document.getElementById("login-username").value;
        const password = document.getElementById("login-password").value;

        try {
            const response = await fetch("http://localhost:8080/auth/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ username, password })
            });

            if (!response.ok) {
                const errorResult = await response.json();
                alert("Login failed: " + errorResult.message);
                return;
            }

            const result = await response.json();
            localStorage.setItem("token", result.token); // Lưu token vào localStorage
            window.location.href = result.redirect; // Chuyển hướng sau đăng nhập

        } catch (error) {
            console.error("Error during login:", error);
            alert("An error occurred during login.");
        }
    });



    // Xử lý đăng ký
    const registerForm = document.getElementById("registerForm");
    registerForm.addEventListener("submit", async function (event) {
        event.preventDefault();

        const username = document.getElementById("register-username").value;
        const email = document.getElementById("register-email").value;
        const password = document.getElementById("register-password").value;

        try {
            const response = await fetch("http://localhost:8080/auth/register", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ username, email, password })
            });
            const result = await response.json();
            if (response.ok) {
                alert("Registration successful! You can now login.");
            } else {
                alert("Registration failed: " + result.message);
            }
        } catch (error) {
            console.error("Error during registration:", error);
        }
    });
});
