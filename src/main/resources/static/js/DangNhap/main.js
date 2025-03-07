document.addEventListener("DOMContentLoaded", function () {
    const container = document.querySelector('.container');
    const registerBtn = document.querySelector('.register-btn');
    const loginBtn = document.querySelector('.login-btn');

    if (container && registerBtn && loginBtn) {
        registerBtn.addEventListener('click', () => container.classList.add('active'));
        loginBtn.addEventListener('click', () => container.classList.remove('active'));
    }

    // Hàm lấy token từ localStorage và tự động thêm vào header Authorization
    const setAuthHeaders = () => {
        const token = localStorage.getItem("token"); // Lấy token từ localStorage
        if (token) {
            return { "Authorization": "Bearer ${token}" };  // Gửi token trong header
        }
        return {};  // Nếu không có token, không gửi header Authorization
    };

    const generateHeader = (headers) => {
        return {
            ...headers,  // Thêm các headers hiện có vào (nếu có)
        };
    };

    // Xử lý form login
    const loginForm = document.getElementById("loginForm");
    if (loginForm) {
        loginForm.addEventListener("submit", async function (event) {
            event.preventDefault();

            const email = document.getElementById("login-email").value;
            const password = document.getElementById("login-password").value;

            try {
                const response = await fetch("http://localhost:8080/auth/login", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                        setAuthHeaders,
                    },
                    body: JSON.stringify({ email, password })
                });

                const text = await response.text();
                console.log("Server response:", text);

                if (!text) {
                    alert("Lỗi: Server không trả về dữ liệu.");
                    return;
                }

                let result;
                try {
                    result = JSON.parse(text);
                } catch (error) {
                    console.error("Lỗi khi parse JSON:", error);
                    alert("Lỗi: Server trả về dữ liệu không hợp lệ.");
                    return;
                }

                // Lưu token và role vào localStorage
                if (response.ok && result.token && result.role && result.redirect) {
                    localStorage.setItem("token", result.token); // Lưu token vào localStorage
                    localStorage.setItem("role", result.role);  // Lưu role vào localStorage

                    // Tự động thêm header Authorization vào các yêu cầu API sau này
                    // Ví dụ, gọi một API yêu cầu Authorization
                    const protectedResponse = await fetch("http://localhost:8080/protected-endpoint", {
                        method: "GET",
                        headers: generateHeader(setAuthHeaders()),  // Thêm Authorization header
                    });

                    const protectedData = await protectedResponse.json();
                    console.log("Dữ liệu bảo vệ nhận được:", protectedData);

                    // Điều hướng người dùng đến trang sau khi đăng nhập thành công
                    window.location.href = result.redirect;  // Điều hướng sang trang sau khi đăng nhập
                } else {
                    alert("Lỗi: Dữ liệu từ server không hợp lệ.");
                }
            } catch (error) {
                console.error("Lỗi khi đăng nhập:", error);
                alert("Đã xảy ra lỗi trong quá trình đăng nhập.");
            }
        });
    }




// Xử lý form register
    const registerForm = document.getElementById("registerForm");
    if (registerForm) {
        registerForm.addEventListener("submit", async function (event) {
            event.preventDefault();

            const username = document.getElementById("register-username").value;
            const email = document.getElementById("register-email").value;
            const password = document.getElementById("register-password").value;
            const role = document.getElementById("register-role").value || "";

            try {
                const response = await fetch("http://localhost:8080/auth/register", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({ username, email, password, role })
                });

                const text = await response.text(); // Lấy phản hồi dạng text để debug
                console.log("Response từ server:", text);

                if (!text) {
                    alert("Lỗi: Server không trả về dữ liệu.");
                    return;
                }

                let result;
                try {
                    result = JSON.parse(text); // Chỉ parse nếu server trả về JSON
                } catch (parseError) {
                    console.error("Lỗi khi parse JSON:", parseError);
                    alert("Lỗi: Server không trả về JSON hợp lệ.");
                    return;
                }

                if (response.ok) {
                    alert("Đăng ký thành công! Hãy đăng nhập.");
                    // Sau khi đăng ký thành công, có thể chuyển hướng đến trang đăng nhập
                    window.location.href = '/auth/login'; // Điều hướng đến trang login
                } else {
                    alert("Lỗi đăng ký: " + (result.message || "Không xác định"));
                }
            } catch (error) {
                console.error("Lỗi khi đăng ký:", error);
                alert("Đã xảy ra lỗi kết nối đến server.");
            }
        });
    }

});
