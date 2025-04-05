document.addEventListener("DOMContentLoaded", function () {
    fetch("/api/hoa-don/list-all", { method: "GET", credentials: "include" })
        .then(response => {
            if (!response.ok) {
                if (response.status === 401) {
                    document.getElementById("donHangContainer").innerHTML = "<p class='text-center text-danger'>Bạn cần đăng nhập để xem hóa đơn.</p>";
                }
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(apiResponse => {
            if (apiResponse.success && Array.isArray(apiResponse.data)) {
                // Sắp xếp danh sách hóa đơn theo ngày tạo từ gần nhất đến lâu nhất
                const sortedData = apiResponse.data.sort((a, b) => {
                    const dateA = new Date(a.ngayTao);
                    const dateB = new Date(b.ngayTao);
                    return dateB - dateA; // So sánh ngày để sắp xếp từ gần nhất đến lâu nhất
                });

                renderDonHangCards(sortedData);

                // Bắt đầu nghe sự kiện scroll
                setupInfiniteScroll(sortedData);
            } else {
                console.error("❌ Dữ liệu không đúng định dạng hoặc lỗi server:", apiResponse);
            }
        })
        .catch(error => {
            console.error("❌ Lỗi khi gọi API đơn hàng:", error);
            document.getElementById("donHangContainer").innerHTML = "<p class='text-center text-danger'>Có lỗi xảy ra khi tải dữ liệu.</p>";
        });
});

let currentDataIndex = 0; // Biến theo dõi vị trí hiện tại của dữ liệu đã hiển thị

function renderDonHangCards(data) {
    const container = document.getElementById("donHangContainer");
    const row = document.getElementById("donHangCardsRow");
    row.innerHTML = "";

    if (!data.length) {
        row.innerHTML = "<p class='text-center'>Không có đơn hàng nào.</p>";
        return;
    }

    // Hiển thị 3 hóa đơn đầu tiên (có ngày tạo gần nhất)
    const displayedData = data.slice(currentDataIndex, currentDataIndex + 10);
    currentDataIndex += 10;

    displayedData.forEach(hoaDon => {
        const card = document.createElement("div");
        card.className = "card shadow-sm mb-4";  // Thẻ card
        card.style.width = "100%";  // Kéo dài thẻ card ra hết chiều rộng container

        // Format số tiền
        const formatMoney = (amount) => new Intl.NumberFormat("vi-VN").format(amount) + " VND";

        card.innerHTML = `
<div class="card-body">
    <!-- Thông tin Mã Hóa Đơn và Tên Khách Hàng -->
    <div class="row mb-3">
        <div class="col-md-6">
            <strong>Mã Hóa Đơn:</strong> <span>${hoaDon.maHoaDon || 'N/A'}</span>
        </div>
        <div class="col-md-6">
            <strong>Tên Khách Hàng:</strong> <span>${hoaDon.nguoiNhan || 'Khách lẻ'}</span>
        </div>
    </div>

    <!-- Thông tin Tên Nhân Viên và Loại Hóa Đơn -->
    <div class="row mb-3">
        <div class="col-md-6">
            <strong>Tên Nhân Viên:</strong> <span>${hoaDon.nguoiTao || 'N/A'}</span>
        </div>
        <div class="col-md-6">
            <strong>Loại Hóa Đơn:</strong> <span>${hoaDon.loaiHoaDon || 'Không có dữ liệu'}</span>
        </div>
    </div>

    <!-- Thông tin Ngày Tạo và Tiền Giảm -->
    <div class="row mb-3">
        <div class="col-md-6">
            <strong>Ngày Tạo:</strong> <span>${hoaDon.ngayTao ? new Date(hoaDon.ngayTao).toLocaleString() : 'Không rõ ngày'}</span>
        </div>
        <div class="col-md-6">
            <strong>Tiền Giảm:</strong> <span>${hoaDon.phieuGiamGia ? formatMoney(hoaDon.phieuGiamGia.giaTriGiam) : '0 VND'}</span>
        </div>
    </div>

    <!-- Thông tin Tổng Tiền -->
<div class="d-flex justify-content-center mt-3">
            <strong>Tổng Tiền: </strong> <span class="text-danger" style="margin-left: 10px;">  ${hoaDon.tongTien ? formatMoney(hoaDon.tongTien) : '0 VND'}</span>
        </div>

    <!-- Nút Chi Tiết - Căn giữa và có icon -->
<div class="d-flex justify-content-center mt-3">
    <a href="/don-hang?maHoaDon=${hoaDon.maHoaDon}" class="btn btn-primary btn-sm fw-bold">
        <i class="bi bi-pencil-square me-2"></i> Chi Tiết
    </a>
</div>

</div>
        `;
        row.appendChild(card);
    });
}

function setupInfiniteScroll(data) {
    // Lắng nghe sự kiện scroll của cửa sổ
    window.addEventListener('scroll', () => {
        // Kiểm tra xem người dùng đã cuộn đến gần cuối trang chưa
        if (window.innerHeight + window.scrollY >= document.body.scrollHeight - 200) {
            if (currentDataIndex < data.length) {
                renderDonHangCards(data);
            }
        }
    });
}
