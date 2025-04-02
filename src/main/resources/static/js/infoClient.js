document.addEventListener("DOMContentLoaded", function () {
    fetch("/api/khach-hang/info", {
        method: "GET",
        credentials: "include" // Để gửi cookie session
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                const user = data.user;

                // Gán dữ liệu vào form
                document.querySelector("input[name='hoTen']").value = user.hoTen;
                document.querySelector("input[name='email']").value = user.email;
                document.querySelector("input[name='soDienThoai']").value = user.soDienThoai;
                document.querySelector("select[name='gioiTinh']").value = user.gioiTinh;
                document.querySelector("input[name='ngaySinh']").value = user.ngaySinh;
                // Hiển thị ảnh đại diện nếu có
                if (user.hinhAnh && user.hinhAnh.trim() !== "") {
                    document.getElementById("avatarPreview").src = user.hinhAnh;
                } else {
                    document.getElementById("avatarPreview").src = "default-avatar.png"; // Ảnh mặc định nếu không có
                }

            } else {
                alert("Vui lòng đăng nhập để xem thông tin!");
            }
        })
        .catch(error => console.error("Lỗi khi lấy thông tin khách hàng:", error));
});

function updateThongTin() {
    const hoTen = document.querySelector("input[name='hoTen']").value;
    const soDienThoai = document.querySelector("input[name='soDienThoai']").value;
    const gioiTinh = document.querySelector("select[name='gioiTinh']").value;
    const ngaySinh = document.querySelector("input[name='ngaySinh']").value;
    const avatarInput = document.querySelector("input[name='hinhAnh']");

    const formData = new FormData();
    formData.append('hoTen', hoTen);
    formData.append('soDienThoai', soDienThoai);
    formData.append('gioiTinh', gioiTinh);
    formData.append('ngaySinh', ngaySinh);

    // Nếu có chọn ảnh mới, thêm ảnh vào formData
    if (avatarInput.files.length > 0) {
        const file = avatarInput.files[0];
        formData.append('file', file);
    }

    sendUpdateRequest(formData);
}

function sendUpdateRequest(formData) {
    fetch("/api/khach-hang/update", {
        method: "PUT",
        credentials: "include",
        body: formData
    })
        .then(response => response.json())
        .then(result => {
            if (result.success) {
                Swal.fire({
                    title: 'Thành công!',
                    text: 'Cập nhật thông tin thành công.',
                    icon: 'success',
                    confirmButtonText: 'OK'
                }).then(() => {
                    window.location.reload(); // Làm mới trang sau khi cập nhật thành công
                });
            } else {
                Swal.fire({
                    title: 'Lỗi!',
                    text: result.message,
                    icon: 'error',
                    confirmButtonText: 'OK'
                });
            }
        })
        .catch(error => console.error("Lỗi cập nhật thông tin:", error));
}

function previewImage(event) {
    const input = event.target;
    const preview = document.getElementById('avatarPreview');

    if (input.files && input.files[0]) {
        const reader = new FileReader();
        reader.onload = function (e) {
            if (preview) preview.src = e.target.result;
        };
        reader.readAsDataURL(input.files[0]);
    }

}

