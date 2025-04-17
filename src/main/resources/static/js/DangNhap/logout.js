document.addEventListener("DOMContentLoaded", function () {
    // Lấy cả hai nút logout
    const logoutBtnKhachHang = document.getElementById("logout-btn-khachhang");
    const logoutBtnNhanVien = document.getElementById("logout-btn-nhanvien");

    // Hàm xử lý logout chung
    async function handleLogout(roles) {
        let redirectUrl = null;

        if (roles.includes("ROLE_KHACHHANG")) {
            redirectUrl = "http://localhost:8080/trang-chu";
        } else if (roles.includes("ROLE_ADMIN") || roles.includes("ROLE_NHANVIEN")) {
            redirectUrl = "http://localhost:8080/auth/home";
        }

        if (!redirectUrl) {
            console.warn("Không xác định được trang chuyển hướng, về trang mặc định.");
            redirectUrl = "http://localhost:8080/trang-chu";
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
                timer: 2000,
                showConfirmButton: false
            }).then(() => {
                window.location.href = redirectUrl;
                setTimeout(() => {
                    location.reload(true);
                }, 1000);
            });
        } else {
            throw new Error("Không thể đăng xuất. Vui lòng thử lại.");
        }
    }

    // Hàm kiểm tra session
    async function checkSessionAndLogout() {
        try {
            // Lấy thông tin session trước khi đăng xuất
            const sessionResponse = await fetch("http://localhost:8080/auth/check-session", {
                method: "GET",
                credentials: "include"
            });

            if (sessionResponse.ok) {
                const sessionData = await sessionResponse.json();
                console.log("Session Data:", sessionData);
                const roles = sessionData.roles || [];

                // Thực hiện đăng xuất theo vai trò
                await handleLogout(roles);
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
    }

    // Kiểm tra nếu nút logout cho khách hàng có
    if (logoutBtnKhachHang) {
        logoutBtnKhachHang.addEventListener("click", async function (e) {
            e.preventDefault();
            await checkSessionAndLogout();
        });
    } else {
        console.warn("Không tìm thấy nút logout cho khách hàng.");
    }

    // Kiểm tra nếu nút logout cho nhân viên/admin có
    if (logoutBtnNhanVien) {
        logoutBtnNhanVien.addEventListener("click", async function (e) {
            e.preventDefault();
            await checkSessionAndLogout();
        });
    } else {
        console.warn("Không tìm thấy nút logout cho nhân viên/admin.");
    }
});
