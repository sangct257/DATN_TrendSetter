document.addEventListener("DOMContentLoaded", function () {
    let currentPage = 0;
    const pageSize = 7;

    function fetchPhieuGiamGia(page) {
        fetch(`/api/phieu-giam-gia?page=${page}`)
            .then(response => response.json())
            .then(data => {
                renderTable(data.content);
                renderPagination(data.totalPages, page);
            })
            .catch(error => console.error("Lỗi khi tải dữ liệu:", error));
    }

    function formatDate(dateString) {
        const date = new Date(dateString);
        return date.toLocaleDateString("vi-VN");
    }
    function getStatus(ngayBatDau) {
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        const startDate = new Date(ngayBatDau);
        startDate.setHours(0, 0, 0, 0);

        if (startDate > today) return "Sắp Diễn Ra";
        if (startDate.getTime() === today.getTime()) return "Đang Hoạt Động";
        return "Ngừng Hoạt Động";
    }
    function renderTable(phieuGiamGiaList) {
        const tbody = document.querySelector("#productTable tbody");
        tbody.innerHTML = phieuGiamGiaList.map((item, index) => `
            <tr class="text-center">
                <td>${index + 1 + currentPage * pageSize}</td>
                <td>${item.maPhieuGiamGia}</td>
                <td>${item.tenPhieuGiamGia}</td>
                <td>${item.giaTriGiam}${item.donViTinh}</td>
                <td>${item.soLuotSuDung}</td>
                <td>${formatDate(item.ngayBatDau)} - ${formatDate(item.ngayKetThuc)}</td>
                <td>
                    <span class="badge ${getStatus(item.ngayBatDau) === 'Đang Hoạt Động' ? 'bg-success' :
            getStatus(item.ngayBatDau) === 'Sắp Diễn Ra' ? 'bg-warning' : 'bg-danger'}">
                        ${getStatus(item.ngayBatDau)}
                    </span>
                </td>
                <td>
                    <button class="btn btn-primary btn-sm" onclick="viewDetail(${item.id})">
                        <i class="bi bi-eye"></i>
                    </button>
                    <button class="btn btn-danger btn-sm" onclick="deletePhieuGiamGia(${item.id})">
                        <i class="bi bi-trash"></i>
                    </button>
                </td>
            </tr>
        `).join("");
    }
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

                // Lưu lại danh sách khách hàng được chọn để khi mở modal lại không bị mất
                const selectedKhachHangs = new Set(data.khachHangIds);

                document.querySelectorAll("#khachHangTable input[type='checkbox']").forEach(checkbox => {
                    let khachHangId = parseInt(checkbox.value);
                    checkbox.checked = selectedKhachHangs.has(khachHangId);
                });

                // Đánh dấu modal đang ở chế độ "Sửa"
                let addModal = new bootstrap.Modal(document.getElementById("addModal"));
                document.getElementById("addModal").setAttribute("data-mode", "edit");

                // Hiển thị lại nút "Sửa" khi ở chế độ sửa
                document.querySelector("#addModal .btn-primary").style.display = "block";
                document.getElementById("modalTitle").textContent = "Sửa Phiếu Giảm Giá"; // Đổi tiêu đề modal
                let btnSubmit = document.querySelector("#addModal .btn-primary");
                btnSubmit.textContent = "Sửa Phiếu Giảm Giá"; // Đổi nút từ "Thêm" thành "Sửa"
                btnSubmit.setAttribute("data-mode", "edit"); // Gán chế độ sửa

                addModal.show();
            })
            .catch(error => console.error("Lỗi khi lấy dữ liệu chi tiết:", error));
    }

    function deletePhieuGiamGia(id) {
        Swal.fire({
            title: "Bạn có chắc chắn muốn xóa?",
            text: "Phiếu giảm giá này sẽ bị xóa !",
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
                            fetchPhieuGiamGia(0);
                        });
                    })
                    .catch(() => {
                        Swal.fire({ title: "Lỗi!", text: "Đã xảy ra lỗi khi xóa.", icon: "error" });
                    });
            }
        });
    }


    function renderPagination(totalPages, currentPage) {
        const pagination = document.querySelector("#pagination");
        pagination.innerHTML = Array.from({ length: totalPages }, (_, i) => `
            <button class="btn ${i === currentPage ? 'btn-primary' : 'btn-outline-primary'} mx-1"
                    onclick="fetchPhieuGiamGia(${i})">${i + 1}</button>
        `).join("");
    }
    fetchPhieuGiamGia(currentPage);
    window.fetchPhieuGiamGia = fetchPhieuGiamGia;
    function setMinDate() {
        const today = new Date().toISOString().split("T")[0];
        document.getElementById("ngayBatDau").setAttribute("min", today);
        document.getElementById("ngayKetThuc").setAttribute("min", today);
    }

    function updateTrangThai() {
        const ngayBatDauInput = document.getElementById("ngayBatDau");
        const trangThaiSelect = document.getElementById("trangThai");

        if (!ngayBatDauInput.value) return; // Nếu chưa chọn ngày, không làm gì

        const ngayBatDau = new Date(ngayBatDauInput.value);
        const today = new Date();

        // Chuyển ngày về dạng YYYY-MM-DD để so sánh chính xác hơn
        const ngayBatDauFormatted = ngayBatDau.toISOString().split("T")[0];
        const todayFormatted = today.toISOString().split("T")[0];

        if (ngayBatDauFormatted === todayFormatted) {
            trangThaiSelect.value = "Đang Hoạt Động";
        } else if (ngayBatDauFormatted > todayFormatted) {
            trangThaiSelect.value = "Sắp diễn ra";
        } else {
            trangThaiSelect.value = "Ngừng Hoạt Động";
        }
    }

    document.getElementById("ngayBatDau").addEventListener("change", function () {
        document.getElementById("ngayKetThuc").setAttribute("min", this.value);
        updateTrangThai(); // Cập nhật trạng thái
    });



    setMinDate();
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
            if (!value || (validate && !validate(value))) showError(id, message);
        });

        const ngayBatDau = new Date(document.getElementById("ngayBatDau").value);
        const ngayKetThuc = new Date(document.getElementById("ngayKetThuc").value);
        if (ngayBatDau > ngayKetThuc) showError("ngayKetThuc", "Ngày kết thúc phải sau ngày bắt đầu.");
        const giaTriGiam = parseFloat(document.getElementById("giaTriGiam").value);
        const dieuKien = parseFloat(document.getElementById("dieuKien").value) || 0;

        if (giaTriGiam > dieuKien) {
            showError("giaTriGiam", "Giá trị giảm không được lớn hơn giá trị tối thiểu.");
        }
        return isValid;
    }
    document.getElementById("searchKhachHang").addEventListener("input", function () {
        const keyword = this.value.trim();
        fetch(`/api/khach-hang/search?keyword=${encodeURIComponent(keyword)}`)
            .then(response => response.json())
            .then(data => {
                const khachHangTable = document.getElementById("khachHangTable");
                khachHangTable.innerHTML = data.map(kh => `
                <tr>
                    <td><input type="checkbox" value="${kh.id}"></td>
                    <td>${kh.hoTen}</td>
                    <td>${kh.email}</td>
                    <td>${kh.soDienThoai}</td>
                </tr>
            `).join("");
            })
            .catch(error => console.error("Lỗi khi tìm kiếm khách hàng:", error));
    });


    function fetchKhachHang() {
        fetch("/api/khach-hang")
            .then(response => response.json())
            .then(data => {
                const khachHangTable = document.getElementById("khachHangTable");
                if (!khachHangTable) return console.error("Lỗi: Không tìm thấy bảng khách hàng!");
                khachHangTable.innerHTML = data.map(kh => `
                    <tr>
                        <td><input type="checkbox" value="${kh.id}"></td>
                        <td>${kh.hoTen}</td>
                        <td>${kh.email}</td>
                        <td>${kh.soDienThoai}</td>
                    </tr>
                `).join("");
                document.querySelectorAll(".kh-checkbox").forEach(checkbox => {
                    checkbox.checked = false;
                });
            })
            .catch(error => console.error("Lỗi tải khách hàng:", error));
    }
    document.getElementById("trangThai").addEventListener("mousedown", function (event) {
        event.preventDefault(); // Ngăn không cho mở dropdown
    });
    document.getElementById("btnAdd").addEventListener("click", function () {
        let modal = document.getElementById("addModal");
        modal.setAttribute("data-mode", "add"); // Đặt mode ngay từ khi bấm mở modal
    });

    function generateMaPhieuGiamGia() {
        return "PGG" + Math.floor(Math.random() * 10000);
    }

    document.getElementById("addModal").addEventListener("show.bs.modal", function () {
        let mode = this.getAttribute("data-mode");

        if (!mode) {
            this.setAttribute("data-mode", "add");
            mode = "add";
        }

        let maPhieuInput = document.getElementById("maPGG");

        if (mode === "add") {
            if (!maPhieuInput.value) {
                maPhieuInput.value = generateMaPhieuGiamGia();
            }

            // Reset form nhưng giữ lại mã phiếu
            document.querySelectorAll("#addPGGForm input, #addPGGForm textarea, #addPGGForm select")
                .forEach(input => {
                    if (input.id !== "maPGG") input.value = "";
                });

            // Hiển thị lại nút "Thêm" khi mở modal ở chế độ thêm
            document.querySelector("#addModal .btn-primary").style.display = "block";

            // Reset checkbox khi mở modal
            document.querySelectorAll("#khachHangTable input[type='checkbox']").forEach(checkbox => {
                checkbox.checked = false;
            });

            // Đổi tiêu đề và nút cho chế độ thêm
            document.getElementById("modalTitle").textContent = "Thêm Phiếu Giảm Giá";
            btnSubmit.textContent = "Thêm Phiếu Giảm Giá";
            btnSubmit.setAttribute("data-mode", "add"); // Gán chế độ thêm
        } else if (mode === "edit") {
            // Đảm bảo modal được chuẩn bị cho chế độ sửa
            document.getElementById("modalTitle").textContent = "Sửa Phiếu Giảm Giá";
            btnSubmit.textContent = "Sửa Phiếu Giảm Giá";
            btnSubmit.setAttribute("data-mode", "edit");
        }
    });




// Khi đóng modal, reset về trạng thái mặc định
    document.getElementById("addModal").addEventListener("hidden.bs.modal", function () {
        this.setAttribute("data-mode", "add");
        document.getElementById("maPGG").value = ""; // Reset mã phiếu khi đóng modal
    });


    document.querySelector("#addModal .btn-primary").addEventListener("click", function () {
        // Kiểm tra chế độ hiện tại (add hoặc edit)
        const mode = document.getElementById("addModal").getAttribute("data-mode");

        // Nếu chế độ là thêm, thực hiện thêm
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
                            document.querySelector('input[name="donViTinh"]:checked').value : "%",
                        soLuotSuDung: parseInt(document.getElementById("soLuong").value),
                        dieuKien: parseFloat(document.getElementById("dieuKien").value) || 0,
                        ngayBatDau: document.getElementById("ngayBatDau").value,
                        ngayKetThuc: document.getElementById("ngayKetThuc").value,
                        trangThai: document.getElementById("trangThai").value
                    };
                    const selectedKhachHangs = Array.from(document.querySelectorAll("#khachHangTable input[type='checkbox']:checked"))
                        .map(checkbox => parseInt(checkbox.value));

                    if (selectedKhachHangs.length === 0) {
                        Swal.fire({ title: "Lỗi!", text: "Vui lòng chọn ít nhất một khách hàng!", icon: "error" });
                        return;
                    }
                    document.querySelectorAll('input[name="donViTinh"]').forEach(radio => {
                        radio.addEventListener("change", function() {
                            console.log("Radio được chọn:", this);
                            console.log("Giá trị đơn vị tính:", this.value);
                        });
                    });

                    fetch("http://localhost:8080/api/phieu-giam-gia/add/multiple", {
                        method: "POST",
                        headers: { "Content-Type": "application/json" },
                        body: JSON.stringify({ phieuGiamGia, khachHangIds: selectedKhachHangs })
                    })
                        .then(response => {
                            if (!response.ok) throw new Error("Có lỗi khi thêm phiếu giảm giá!");
                            return response.json();
                        })
                        .then(() => {
                            Swal.fire({ title: "Thành công!", text: "Phiếu giảm giá đã được thêm.", icon: "success" })
                                .then(() => {
                                    document.getElementById("addPGGForm").reset();
                                    fetchPhieuGiamGia(0);
                                    let modal = bootstrap.Modal.getInstance(document.getElementById("addModal"));
                                    modal.hide();
                                    setTimeout(() => {
                                        document.querySelectorAll(".modal-backdrop").forEach(el => el.remove());
                                        document.body.classList.remove("modal-open");
                                    }, 300);
                                });
                        })

                        .catch(() => {
                            Swal.fire({ title: "Lỗi!", text: "Đã xảy ra lỗi khi thêm phiếu giảm giá.", icon: "error" });
                        });
                }
            });
        }
        // Nếu chế độ là sửa, thực hiện sửa
        else if (mode === "edit") {
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

                    const selectedKhachHangs = Array.from(document.querySelectorAll("#khachHangTable input[type='checkbox']:checked"))
                        .map(checkbox => parseInt(checkbox.value));

                    if (selectedKhachHangs.length === 0) {
                        Swal.fire({ title: "Lỗi!", text: "Vui lòng chọn ít nhất một khách hàng!", icon: "error" });
                        return;
                    }

                    const id = document.getElementById("phieuGiamGiaId").value;
                    console.log("ID cần cập nhật:", id); // Kiểm tra ID trước khi gửi API

                    fetch(`http://localhost:8080/api/phieu-giam-gia/update/${id}`, {
                        method: "PUT",
                        headers: { "Content-Type": "application/json" },
                        body: JSON.stringify({ phieuGiamGia, khachHangIds: selectedKhachHangs })
                    })
                        .then(response => {
                            if (!response.ok) throw new Error("Có lỗi khi cập nhật phiếu giảm giá!");
                            return response.json();
                        })
                        .then(() => {
                            Swal.fire({ title: "Thành công!", text: "Phiếu giảm giá đã được cập nhật.", icon: "success" })
                                .then(() => {
                                    fetchPhieuGiamGia(0);
                                    let modal = bootstrap.Modal.getInstance(document.getElementById("addModal"));
                                    modal.hide();

                                    setTimeout(() => {
                                        document.querySelectorAll(".modal-backdrop").forEach(el => el.remove());
                                        document.body.classList.remove("modal-open");
                                    }, 300);
                                });
                        })
                        .catch(() => {
                            Swal.fire({ title: "Lỗi!", text: "Đã xảy ra lỗi khi cập nhật phiếu giảm giá.", icon: "error" });
                        });
                }

            });
        }
    });



    fetchPhieuGiamGia(currentPage);
    fetchKhachHang();
    window.viewDetail = viewDetail;
    window.deletePhieuGiamGia = deletePhieuGiamGia;

});
