document.getElementById("logout-btn").addEventListener("click", async function (e) {
    e.preventDefault(); // Ngăn chặn hành động mặc định

    const response = await fetch("http://localhost:8080/api/auth/logout", {
        method: "GET",
        credentials: "include"
    });

    if (response.ok) {
        Swal.fire({
            title: "Đăng xuất thành công!",
            icon: "success",
            timer: 2000, // 2 giây tự động đóng
            showConfirmButton: false
        }).then(() => {
            window.location.replace("http://localhost:8080/auth/trendsetter"); // Chuyển hướng sau khi thông báo đóng
        });
    } else {
        Swal.fire({
            title: "Có lỗi xảy ra!",
            text: "Không thể đăng xuất. Vui lòng thử lại.",
            icon: "error",
            confirmButtonText: "OK"
        });
    }
});
