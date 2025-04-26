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

        // Điều kiện hiển thị nút Huỷ đơn
     const canCancel = ["Chờ Xác Nhận", "Đã Xác Nhận", "Chờ Vận Chuyển"].includes(hoaDon.trangThai)
         && hoaDon.idPhuongThucThanhToan !== 2
         && hoaDon.idPhuongThucThanhToan !== 3;

     const cancelButton = canCancel
         ? `<button class="btn btn-danger btn-sm fw-bold me-2" onclick="moModalHuy('${hoaDon.id}')">Huỷ đơn</button>`
         : "";



        card.innerHTML = `
<div class="card-body">
    <!-- Thông tin Mã Hóa Đơn và Tên Khách Hàng -->
    <div class="row mb-3">
        <div class="col-md-6">
            <strong>Mã Hóa Đơn:</strong> <span>${hoaDon.maHoaDon || 'N/A'}</span>
        </div>
        <div class="col-md-6">
            <strong>Người Nhận</strong> <span>${hoaDon.nguoiNhan || 'Khách lẻ'}</span>
        </div>
    </div>

    <!-- Thông tin Tên Nhân Viên và Loại Hóa Đơn -->
    <div class="row mb-3">
        <div class="col-md-6">
             <strong>Ngày Tạo:</strong> <span>${hoaDon.ngayTao ? new Date(hoaDon.ngayTao).toLocaleString() : 'Không rõ ngày'}</span>
        </div>
        <div class="col-md-6">
            <strong>Loại Hóa Đơn:</strong> <span>${hoaDon.loaiHoaDon || 'Không có dữ liệu'}</span>
        </div>
    </div>

    <!-- Thông tin Tổng Tiền -->
        <div class="row mb-3">
            <div class="col-md-6">
                <strong>Tổng Tiền: </strong> <span class="text-danger" style="margin-left: 10px;">  ${hoaDon.tongTien ? formatMoney(hoaDon.tongTien) : '0 VND'}</span>
            </div>
            <div class="col-md-6">
                <strong>Trạng Thái: </strong> <span class="text-danger" style="margin-left: 10px;">  ${hoaDon.trangThai}</span>
            </div>
        </div>

    <!-- Nút Chi Tiết - Căn giữa và có icon -->
<div class="d-flex justify-content-center mt-3">
    <!-- Nút hủy có thêm margin-end -->
    ${cancelButton.replace('class="', 'class="me-2 ')}

    <!-- Nút Chi Tiết -->
    <a href="/don-hang?maHoaDon=${hoaDon.maHoaDon}" class="btn btn-primary btn-sm fw-bold">
        <i class="bi bi-pencil-square me-2"></i> Chi Tiết
    </a>
</div>



<!-- Modal chọn lý do huỷ đơn -->
<div class="modal fade" id="modalHuyDon" tabindex="-1" aria-labelledby="modalHuyDonLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header bg-danger text-white">
        <h5 class="modal-title" id="modalHuyDonLabel">Xác nhận hủy đơn</h5>
        <button type="button" class="close" data-dismiss="modal" aria-label="Đóng">
          <span aria-hidden="true">&times;</span>
        </button>
      </div>
      <div class="modal-body">
        <input type="hidden" id="hoaDonIdHuy">
        <div class="mb-3">
          <label for="lyDoHuy" class="form-label">Lý do hủy đơn:</label>
          <select id="lyDoHuy" class="form-select">
            <option value="Khách đổi ý">Khách đổi ý</option>
            <option value="Sản phẩm không đúng mô tả">Sản phẩm không đúng mô tả</option>
            <option value="Giao hàng quá chậm">Giao hàng quá chậm</option>
            <option value="Lý do khác">Lý do khác</option>
          </select>
        </div>
        <div class="mb-3">
          <label for="ghiChuKhac" class="form-label">Ghi chú thêm (nếu có):</label>
          <textarea id="ghiChuKhac" class="form-control" rows="2"></textarea>
        </div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-dismiss="modal">Đóng</button>
        <button type="button" class="btn btn-danger" onclick="xacNhanHuy()">Xác nhận huỷ</button>
      </div>
    </div>
  </div>
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

function moModalHuy(hoaDonId) {
    document.getElementById('hoaDonIdHuy').value = hoaDonId;
    document.getElementById('lyDoHuy').value = "Khách đổi ý";
    document.getElementById('ghiChuKhac').value = "";
    new bootstrap.Modal(document.getElementById('modalHuyDon')).show();
  }

function xacNhanHuy() {
    const hoaDonId = document.getElementById('hoaDonIdHuy').value;
    const lyDo = document.getElementById('lyDoHuy').value;
    const ghiChuThem = document.getElementById('ghiChuKhac').value;
    const ghiChu = ghiChuThem ? lyDo + " - " + ghiChuThem : lyDo;

    fetch("/api/v2/huy", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({ hoaDonId: hoaDonId, ghiChu: ghiChu })
    })
    .then(res => res.json())
    .then(data => {
      if (data.success) {
        Swal.fire({
          icon: 'success',
          title: 'Hủy đơn thành công!',
          text: 'Đơn hàng đã được cập nhật trạng thái.',
          confirmButtonText: 'OK'
        }).then(() => {
          location.reload();
        });
      } else {
        Swal.fire({
          icon: 'error',
          title: 'Hủy thất bại',
          text: data.errorMessage || 'Có lỗi xảy ra!',
          confirmButtonText: 'Thử lại'
        });
      }
    })
    .catch(err => {
      Swal.fire({
        icon: 'error',
        title: 'Lỗi kết nối',
        text: 'Không thể kết nối đến máy chủ!',
        confirmButtonText: 'Đóng'
      });
    });
}
