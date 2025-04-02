document.getElementById("logout-btn").addEventListener("click", async function (e) {
    e.preventDefault(); // Ngăn chặn hành động mặc định

    try {
        // Lấy thông tin session trước khi đăng xuất
        const sessionResponse = await fetch("http://localhost:8080/auth/check-session", {
            method: "GET",
            credentials: "include"
        });

        let redirectUrl = null;

        if (sessionResponse.ok) {
            const sessionData = await sessionResponse.json();
            console.log("Session Data:", sessionData); // Debug thông tin session
            const roles = sessionData.role || [];

            if (roles.includes("ROLE_KHACHHANG")) {
                redirectUrl = "http://localhost:8080/trang-chu";
            } else if (roles.includes("ROLE_ADMIN") || roles.includes("ROLE_NHANVIEN")) {
                redirectUrl = "http://localhost:8080/auth/home";
            }
        }

        if (!redirectUrl) {
            console.warn("Không xác định được trang chuyển hướng, về trang mặc định.");
            redirectUrl = "http://localhost:8080/auth/home"; // Mặc định nếu không xác định được
        }

        // Gửi yêu cầu đăng xuất
        const response = await fetch("http://localhost:8080/auth/logout", {
            method: "POST",
            credentials: "include"
        });

        if (response.ok) {
            Swal.fire({
                title: "Đăng xuất thành công!",
                icon: "success",
                timer: 2000, // 2 giây tự động đóng
                showConfirmButton: false
            }).then(() => {
                window.location.href = redirectUrl;
                setTimeout(() => { location.reload(true); }, 1000); // Đảm bảo reload sau khi chuyển trang
            });
        } else {
            throw new Error("Không thể đăng xuất. Vui lòng thử lại.");
        }
    } catch (error) {
        console.error("Lỗi khi xử lý đăng xuất:", error);
        Swal.fire({
            title: "Có lỗi xảy ra!",
            text: error.message,
            icon: "error",
            confirmButtonText: "OK"
        });
    }
});
