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
                window.location.href = redirectUrl;  // Chỉ chuyển hướng, không reload
            });
        } else {
            throw new Error("Không thể đăng xuất. Vui lòng thử lại.");
        }
    }

    // Hàm kiểm tra session
    async function checkSessionAndLogout(roleClicked) {
    try {
        // Gọi check-session như cũ
        const sessionResponse = await fetch("http://localhost:8080/auth/check-session", {
            method: "GET",
            credentials: "include"
        });

        if (sessionResponse.ok) {
            const sessionData = await sessionResponse.json();
            console.log("Session Data:", sessionData);
            const roles = sessionData.roles || [];

            // Ưu tiên dùng vai trò truyền vào nếu có
            if (roleClicked === "khachhang") {
                await handleLogout(["ROLE_KHACHHANG"]);
            } else if (roleClicked === "nhanvien") {
                if (roles.includes("ROLE_ADMIN")) {
                    await handleLogout(["ROLE_ADMIN"]);
                } else {
                    await handleLogout(["ROLE_NHANVIEN"]);
                }
            } else {
                await handleLogout(roles); // fallback nếu không truyền gì
            }
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
            await checkSessionAndLogout("khachhang"); // chỉ rõ
        });
    }

    if (logoutBtnNhanVien) {
        logoutBtnNhanVien.addEventListener("click", async function (e) {
            e.preventDefault();
            await checkSessionAndLogout("nhanvien"); // chỉ rõ
        });
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
