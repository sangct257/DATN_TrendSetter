let dataTable; // Biến toàn cục để lưu instance của DataTable

document.addEventListener("DOMContentLoaded", function () {

    // Hàm lấy dữ liệu phiếu giảm giá từ API
    function fetchPhieuGiamGia(trangThai = "") {
        let url = '/api/phieu-giam-gia';
        if (trangThai) {
            url += `?trangThai=${encodeURIComponent(trangThai)}`;
        }

        console.log("Fetching from URL: ", url);  // In ra URL API để kiểm tra

        fetch(url)
            .then(response => response.json())
            .then(data => {
                console.log("Data received: ", data);  // In ra dữ liệu nhận được từ API
                if (Array.isArray(data) && data.length > 0) {
                    renderTable(data);
                    loadCount();
                } else {
                    console.error("Dữ liệu rỗng hoặc không hợp lệ");
                    // Nếu cần, bạn có thể xóa dữ liệu bảng khi không có dữ liệu:
                    if(dataTable){
                        dataTable.clear().draw();
                    }
                }
            })
            .catch(error => {
                console.error("Lỗi khi lấy dữ liệu:", error);
            });
    }

    // Hàm chuyển đổi ngày sang định dạng Việt Nam
    function formatDate(dateString) {
        const date = new Date(dateString);
        return date.toLocaleDateString("vi-VN");
    }

    // Hàm render bảng qua DataTable API
    function renderTable(phieuGiamGiaList) {
        if (!Array.isArray(phieuGiamGiaList) || phieuGiamGiaList.length === 0) {
            console.error("Dữ liệu không hợp lệ hoặc rỗng");
            return;
        }

        // Tạo mảng rows cho DataTable
        const rows = phieuGiamGiaList.map((item, index) => {
            return [
                index + 1,
                item.maPhieuGiamGia,
                item.tenPhieuGiamGia,
                `${item.giaTriGiam} ${item.donViTinh}`,
                item.soLuotSuDung,
                `${formatDate(item.ngayBatDau)} - ${formatDate(item.ngayKetThuc)}`,
                // Thay thế <span> thành <button> để người dùng có thể nhấn vào
                `<button class="btn ${
                    item.trangThai === 'Đang Hoạt Động' ? 'btn-success' : 
                        item.trangThai === 'Sắp Diễn Ra' ? 'btn-warning' : 
                            'btn-danger'} btn-sm" 
                                onclick="window.toggleStatus(${item.id}, '${item.trangThai}')">
                            ${item.trangThai}
                </button>`,
                `<button class="btn btn-primary btn-sm" onclick="viewDetail(${item.id})">
                <i class="bi bi-eye"></i>
            </button>
            <button class="btn btn-danger btn-sm" onclick="deletePhieuGiamGia(${item.id})">
                <i class="bi bi-trash"></i>
            </button>`
            ];
        });

        // Nếu DataTable đã được khởi tạo, cập nhật dữ liệu mới
        if (dataTable) {
            dataTable.clear();
            dataTable.rows.add(rows);
            dataTable.draw();
        } else {
            // Khởi tạo DataTable lần đầu
            dataTable = $("#productTable").DataTable({
                data: rows,
                columns: [
                    { title: "#" },
                    { title: "Mã phiếu" },
                    { title: "Tên phiếu" },
                    { title: "Giá trị giảm" },
                    { title: "Số lượt sử dụng" },
                    { title: "Ngày áp dụng" },
                    { title: "Trạng thái" },
                    { title: "Hành động", orderable: false }
                ],
                paging: true,
                lengthMenu: [5, 10, 20],
                pageLength: 5,
                autoWidth: false,
                responsive: true,
                language: {
                    sProcessing: "Đang xử lý...",
                    sLengthMenu: "Hiển thị _MENU_ dòng",
                    sZeroRecords: "Không tìm thấy dữ liệu",
                    sInfo: "Hiển thị _START_ đến _END_ trong tổng _TOTAL_ dòng",
                    sInfoEmpty: "Không có dữ liệu để hiển thị",
                    sInfoFiltered: "(lọc từ _MAX_ dòng)",
                    sSearch: "Tìm kiếm:",
                    oPaginate: {
                        sFirst: "Đầu",
                        sPrevious: "Trước",
                        sNext: "Tiếp",
                        sLast: "Cuối"
                    }
                },
                initComplete: function () {
                    $(".dataTables_length select").css({
                        "font-size": "16px",
                        "padding": "10px 20px"
                    });
                }
            });
        }
    }

    // Sự kiện lọc theo trạng thái khi click vào các button có class filter-btn
    document.querySelectorAll('.filter-btn').forEach(button => {
        button.addEventListener('click', function() {
            const trangThai = this.getAttribute('data-status');
            fetchPhieuGiamGia(trangThai);  // Gọi hàm để tải dữ liệu theo trạng thái
        });
    });

    // Hàm lấy số lượng phiếu theo trạng thái
    function loadCount() {
        fetch("/api/phieu-giam-gia/count")
            .then(response => response.json())
            .then(counts => {
                document.getElementById("count-all").innerText = counts["Tất Cả"] || 0;
                document.getElementById("count-active").innerText = counts["Đang Hoạt Động"] || 0;
                document.getElementById("count-inactive").innerText = counts["Ngừng Hoạt Động"] || 0;
                document.getElementById("count-upcoming").innerText = counts["Sắp Diễn Ra"] || 0;
            })
            .catch(error => console.error("Lỗi:", error));
    }

    window.toggleStatus = function(id, currentStatus) {
        let actionText = currentStatus === "Đang Hoạt Động" ? "vô hiệu hóa" : "kích hoạt lại";

        Swal.fire({
            title: `Xác nhận ${actionText} phiếu giảm giá?`,
            text: `Bạn có chắc muốn ${actionText} phiếu giảm giá này không?`,
            icon: "warning",
            showCancelButton: true,
            confirmButtonColor: "#3085d6",
            cancelButtonColor: "#d33",
            confirmButtonText: "Có, thay đổi!",
            cancelButtonText: "Hủy"
        }).then((result) => {
            if (result.isConfirmed) {
                fetch(`/api/phieu-giam-gia/toggle-status/${id}`, {
                    method: "PUT",
                    headers: {
                        "Content-Type": "application/json"
                    }
                })
                    .then(response => response.json())
                    .then(data => {
                        if (data.message) {
                            Swal.fire({
                                icon: "success",
                                title: "Thành công",
                                text: data.message,
                                confirmButtonText: "OK"
                            }).then(() => {
                                location.reload();
                            });
                        } else {
                            Swal.fire({
                                icon: "error",
                                title: "Lỗi",
                                text: data.error,
                                confirmButtonText: "OK"
                            });
                        }
                    })
                    .catch(error => {
                        console.error("Lỗi:", error);
                        Swal.fire({
                            icon: "error",
                            title: "Lỗi",
                            text: "Đã xảy ra lỗi trong quá trình xử lý!",
                            confirmButtonText: "OK"
                        });
                    });
            }
        });
    };

    // Đảm bảo rằng phần dưới có sự kiện DOMContentLoaded
    document.addEventListener("DOMContentLoaded", function() {
        loadCount();  // Gọi hàm loadCount khi trang đã sẵn sàng
        // Các sự kiện khác có thể đăng ký ở đây nếu cần
    });

    // Hàm hiển thị chi tiết phiếu giảm giá qua modal
    function viewDetail(id) {
        fetch(`/api/phieu-giam-gia/detail/${id}`)
            .then(response => response.json())
            .then(data => {
                if (!data.phieuGiamGia) {
                    console.error("Lỗi: Không tìm thấy dữ liệu phiếu giảm giá!");
                    return;
                }
                const pgg = data.phieuGiamGia;
                document.getElementById("phieuGiamGiaId").value = pgg.id;
                document.getElementById("maPGG").value = pgg.maPhieuGiamGia;
                document.getElementById("tenPGG").value = pgg.tenPhieuGiamGia;
                document.getElementById("moTa").value = pgg.moTa;
                document.getElementById("giaTriGiam").value = pgg.giaTriGiam;
                document.getElementById("soLuong").value = pgg.soLuotSuDung;
                document.getElementById("ngayBatDau").value = pgg.ngayBatDau;
                document.getElementById("ngayKetThuc").value = pgg.ngayKetThuc;
                document.getElementById("trangThai").value = pgg.trangThai;
                document.getElementById("dieuKien").value = pgg.dieuKien;

                // Gọi hàm cập nhật trạng thái sau khi giá trị ngày bắt đầu được thiết lập
                updateTrangThai();

                // Hiển thị modal
                let addModal = new bootstrap.Modal(document.getElementById("addModal"));
                document.getElementById("addModal").setAttribute("data-mode", "edit");
                document.querySelector("#addModal .btn-primary").style.display = "block";
                document.getElementById("modalTitle").textContent = "Sửa Phiếu Giảm Giá";
                let btnSubmit = document.querySelector("#addModal .btn-primary");
                btnSubmit.textContent = "Sửa Phiếu Giảm Giá";
                btnSubmit.setAttribute("data-mode", "edit");

                addModal.show();
            })
            .catch(error => console.error("Lỗi khi lấy dữ liệu chi tiết:", error));
    }

    // Hàm xóa phiếu giảm giá
    function deletePhieuGiamGia(id) {
        Swal.fire({
            title: "Bạn có chắc chắn muốn xóa?",
            text: "Phiếu giảm giá này sẽ bị xóa!",
            icon: "warning",
            showCancelButton: true,
            confirmButtonText: "Xóa",
            cancelButtonText: "Hủy",
            reverseButtons: true
        }).then(result => {
            if (result.isConfirmed) {
                fetch(`/api/phieu-giam-gia/${id}`, { method: "DELETE" })
                    .then(response => {
                        if (!response.ok) throw new Error("Không thể xóa phiếu giảm giá!");
                        return response.text();
                    })
                    .then(() => {
                        Swal.fire({ title: "Xóa thành công!", icon: "success" }).then(() => {
                            // Sau khi xóa thành công, tải lại trang để cập nhật dữ liệu
                            location.reload();
                        });
                    })
                    .catch(() => {
                        Swal.fire({ title: "Lỗi!", text: "Đã xảy ra lỗi khi xóa.", icon: "error" });
                    });
            }
        });
    }


    // Sự kiện thiết lập giá trị min cho ngày
    function setMinDate() {
        const today = new Date().toISOString().split("T")[0];
        document.getElementById("ngayBatDau").setAttribute("min", today);
        document.getElementById("ngayKetThuc").setAttribute("min", today);
    }

    // Cập nhật trạng thái tự động dựa vào ngày bắt đầu
    function updateTrangThai() {
        const ngayBatDauInput = document.getElementById("ngayBatDau");
        const trangThaiSelect = document.getElementById("trangThai");

        if (!ngayBatDauInput.value) return;

        const ngayBatDau = new Date(ngayBatDauInput.value);
        const today = new Date();
        const ngayBatDauFormatted = ngayBatDau.toISOString().split("T")[0];
        const todayFormatted = today.toISOString().split("T")[0];

        if (!trangThaiSelect.value || trangThaiSelect.value === "Sắp Diễn Ra") {
            if (ngayBatDauFormatted === todayFormatted) {
                trangThaiSelect.value = "Đang Hoạt Động";
            } else if (ngayBatDauFormatted > todayFormatted) {
                trangThaiSelect.value = "Sắp Diễn Ra";
            } else {
                trangThaiSelect.value = "Ngừng Hoạt Động";
            }
        }
    }

    document.getElementById("ngayBatDau").addEventListener("change", function () {
        document.getElementById("ngayKetThuc").setAttribute("min", this.value);
        updateTrangThai();
    });

    setMinDate();

    // Hàm validate form
    function validateForm() {
        let isValid = true;
        document.querySelectorAll(".is-invalid").forEach(el => el.classList.remove("is-invalid"));
        document.querySelectorAll(".invalid-feedback").forEach(el => el.remove());

        function showError(inputId, message) {
            const inputElement = document.getElementById(inputId);
            inputElement.classList.add("is-invalid");
            inputElement.insertAdjacentHTML("afterend", `<div class="invalid-feedback">${message}</div>`);
            isValid = false;
        }

        const fields = [
            { id: "maPGG", message: "Vui lòng nhập mã phiếu giảm giá." },
            { id: "tenPGG", message: "Vui lòng nhập tên phiếu giảm giá." },
            { id: "giaTriGiam", message: "Vui lòng nhập giá trị giảm hợp lệ.", validate: v => !isNaN(v) && v > 0 },
            { id: "soLuong", message: "Vui lòng nhập số lượng hợp lệ.", validate: v => !isNaN(v) && v > 0 },
            { id: "ngayBatDau", message: "Vui lòng chọn ngày bắt đầu." },
            { id: "ngayKetThuc", message: "Vui lòng chọn ngày kết thúc." }
        ];

        fields.forEach(({ id, message, validate }) => {
            const value = document.getElementById(id).value.trim();
            if (!value || (validate && !validate(value))) {
                showError(id, message);
            }
        });

        const ngayBatDau = new Date(document.getElementById("ngayBatDau").value);
        const ngayKetThuc = new Date(document.getElementById("ngayKetThuc").value);
        if (ngayBatDau > ngayKetThuc) {
            showError("ngayKetThuc", "Ngày kết thúc phải sau ngày bắt đầu.");
        }
        const giaTriGiam = parseFloat(document.getElementById("giaTriGiam").value);
        const dieuKien = parseFloat(document.getElementById("dieuKien").value) || 0;
        if (giaTriGiam > dieuKien) {
            showError("giaTriGiam", "Giá trị giảm không được lớn hơn giá trị tối thiểu.");
        }
        return isValid;
    }

    // Ngăn dropdown tự mở khi nhấn vào trang thái
    document.getElementById("trangThai").addEventListener("mousedown", function (event) {
        event.preventDefault();
    });

    // Khi nhấn nút "Thêm", đặt modal ở chế độ thêm
    document.getElementById("btnAdd").addEventListener("click", function () {
        const modal = document.getElementById("addModal");
        modal.setAttribute("data-mode", "add");
    });

    // Hàm tạo mã phiếu giảm giá ngẫu nhiên
    function generateMaPhieuGiamGia() {
        return "PGG" + Math.floor(Math.random() * 10000);
    }

    // Sự kiện khi mở modal
    document.getElementById("addModal").addEventListener("show.bs.modal", function () {
        let mode = this.getAttribute("data-mode") || "add";
        this.setAttribute("data-mode", mode);
        const maPhieuInput = document.getElementById("maPGG");
        const btnSubmit = document.querySelector("#addModal .btn-primary");

        if (mode === "add") {
            if (!maPhieuInput.value) {
                maPhieuInput.value = generateMaPhieuGiamGia();
            }
            // Reset các input khác (giữ mã phiếu)
            document.querySelectorAll("#addPGGForm input, #addPGGForm textarea, #addPGGForm select")
                .forEach(input => {
                    if (input.id !== "maPGG") input.value = "";
                });
            document.querySelector("#addModal .btn-primary").style.display = "block";
            document.getElementById("modalTitle").textContent = "Thêm Phiếu Giảm Giá";
            btnSubmit.textContent = "Thêm Phiếu Giảm Giá";
            btnSubmit.setAttribute("data-mode", "add");
        } else if (mode === "edit") {
            document.getElementById("modalTitle").textContent = "Sửa Phiếu Giảm Giá";
            btnSubmit.textContent = "Sửa Phiếu Giảm Giá";
            btnSubmit.setAttribute("data-mode", "edit");
        }
    });

    // Khi đóng modal, reset trạng thái modal
    document.getElementById("addModal").addEventListener("hidden.bs.modal", function () {
        this.setAttribute("data-mode", "add");
        document.getElementById("maPGG").value = "";
    });

    // Sự kiện submit khi nhấn nút trong modal
    document.querySelector("#addModal .btn-primary").addEventListener("click", function () {
        const mode = document.getElementById("addModal").getAttribute("data-mode");

        if (mode === "add") {
            if (!validateForm()) return;

            Swal.fire({
                title: "Xác nhận",
                text: "Bạn có chắc chắn muốn thêm phiếu giảm giá này không?",
                icon: "question",
                showCancelButton: true,
                confirmButtonText: "Yes",
                cancelButtonText: "Cancel",
                reverseButtons: true
            }).then(result => {
                if (result.isConfirmed) {
                    const phieuGiamGia = {
                        maPhieuGiamGia: document.getElementById("maPGG").value,
                        tenPhieuGiamGia: document.getElementById("tenPGG").value,
                        moTa: document.getElementById("moTa").value,
                        giaTriGiam: parseFloat(document.getElementById("giaTriGiam").value),
                        donViTinh: document.querySelector('input[name="donViTinh"]:checked') ?
                            document.querySelector('input[name="donViTinh"]:checked').value : "VND",
                        soLuotSuDung: parseInt(document.getElementById("soLuong").value),
                        dieuKien: parseFloat(document.getElementById("dieuKien").value) || 0,
                        ngayBatDau: document.getElementById("ngayBatDau").value,
                        ngayKetThuc: document.getElementById("ngayKetThuc").value,
                        trangThai: document.getElementById("trangThai").value
                    };
                    fetch("http://localhost:8080/api/phieu-giam-gia/add/multiple", {
                        method: "POST",
                        headers: { "Content-Type": "application/json" },
                        body: JSON.stringify({ phieuGiamGia: phieuGiamGia })  // Chú ý sửa lại chỗ này
                    })
                        .then(response => {
                            if (!response.ok) throw new Error("Có lỗi khi thêm phiếu giảm giá!");
                            return response.json();
                        })
                        .then(() => {
                            Swal.fire({ title: "Thành công!", text: "Phiếu giảm giá đã được thêm.", icon: "success" })
                                .then(() => {
                                    document.getElementById("addPGGForm").reset();
                                    // Sau khi thêm thành công, tải lại trang để cập nhật dữ liệu
                                    location.reload();
                                    let modal = bootstrap.Modal.getInstance(document.getElementById("addModal"));
                                    modal.hide();
                                });
                        })
                        .catch(() => {
                            Swal.fire({ title: "Lỗi!", text: "Đã xảy ra lỗi khi thêm phiếu giảm giá.", icon: "error" });
                        });
                }
            });
        } else if (mode === "edit") {
            if (!validateForm()) return;

            Swal.fire({
                title: "Xác nhận",
                text: "Bạn có chắc chắn muốn cập nhật phiếu giảm giá này không?",
                icon: "question",
                showCancelButton: true,
                confirmButtonText: "Yes",
                cancelButtonText: "Cancel",
                reverseButtons: true
            }).then(result => {
                if (result.isConfirmed) {
                    const phieuGiamGia = {
                        maPhieuGiamGia: document.getElementById("maPGG").value,
                        tenPhieuGiamGia: document.getElementById("tenPGG").value,
                        moTa: document.getElementById("moTa").value,
                        giaTriGiam: parseFloat(document.getElementById("giaTriGiam").value),
                        donViTinh: document.querySelector('input[name="donViTinh"]:checked').value,
                        soLuotSuDung: parseInt(document.getElementById("soLuong").value),
                        dieuKien: parseFloat(document.getElementById("dieuKien").value) || 0,
                        ngayBatDau: document.getElementById("ngayBatDau").value,
                        ngayKetThuc: document.getElementById("ngayKetThuc").value,
                        trangThai: document.getElementById("trangThai").value
                    };

                    const id = document.getElementById("phieuGiamGiaId").value;

                    fetch(`http://localhost:8080/api/phieu-giam-gia/update/${id}`, {
                        method: "PUT",
                        headers: { "Content-Type": "application/json" },
                        body: JSON.stringify({ phieuGiamGia })
                    })
                        .then(response => {
                            if (!response.ok) throw new Error("Có lỗi khi cập nhật phiếu giảm giá!");
                            return response.json();
                        })
                        .then(() => {
                            Swal.fire({ title: "Thành công!", text: "Phiếu giảm giá đã được cập nhật.", icon: "success" })
                                .then(() => {
                                    // Sau khi cập nhật thành công, tải lại trang để cập nhật dữ liệu
                                    location.reload();
                                    let modal = bootstrap.Modal.getInstance(document.getElementById("addModal"));
                                    modal.hide();
                                });
                        })
                        .catch(() => {
                            Swal.fire({ title: "Lỗi!", text: "Đã xảy ra lỗi khi cập nhật phiếu giảm giá.", icon: "error" });
                        });
                }
            });
        }
    });

    // Expose các hàm viewDetail và deletePhieuGiamGia ra global để gọi từ HTML
    window.viewDetail = viewDetail;
    window.deletePhieuGiamGia = deletePhieuGiamGia;

    // Lần đầu tải trang, gọi hàm fetch để hiển thị dữ liệu
    fetchPhieuGiamGia();
    // Cũng expose hàm fetchPhieuGiamGia nếu cần gọi từ ngoài
    window.fetchPhieuGiamGia = fetchPhieuGiamGia;
});
