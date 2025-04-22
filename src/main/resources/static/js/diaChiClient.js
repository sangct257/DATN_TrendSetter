document.addEventListener("DOMContentLoaded", function () {
    fetch("/api/dia-chi", { method: "GET", credentials: "include" })
        .then(response => response.json())
        .then(apiResponse => {
            if (apiResponse && Array.isArray(apiResponse.data)) {
                renderDiaChiCards(apiResponse.data);
            } else {
                console.error("Dữ liệu không đúng định dạng:", apiResponse);
            }
        })
        .catch(error => console.error("Lỗi khi tải danh sách địa chỉ:", error));
});

function renderDiaChiCards(data) {
    const container = document.getElementById("diaChiContainer");
    container.innerHTML = "";

    if (!data.length) {
        container.innerHTML = "<p class='text-center'>Không có địa chỉ nào.</p>";
        return;
    }

    // Sắp xếp các địa chỉ để những địa chỉ có trạng thái 'Mặc Định' lên đầu
    data.sort((a, b) => (b.trangThai === 'Mặc Định' ? 1 : 0) - (a.trangThai === 'Mặc Định' ? 1 : 0));

    data.forEach(diaChi => {
        const card = document.createElement("div");
        card.className = "card shadow-sm";  // Thẻ card
        card.style.width = "100%";  // Kéo dài thẻ card ra hết chiều rộng container
        card.innerHTML = `
            <div class="card-body">
                <h5 class="card-title">${diaChi.diaChiCuThe || "N/A"}</h5>
                <p class="card-text">
                    ${diaChi.phuong || "N/A"}, ${diaChi.huyen || "N/A"}, ${diaChi.thanhPho || "N/A"}
                </p>
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <!-- Label và Switch cho trạng thái mặc định -->
                    <div class="d-flex align-items-center">
                        <label class="form-check-label mr-3" for="switch-${diaChi.id}">Mặc Định</label>
                            <div class="custom-control custom-switch">
                                <input class="custom-control-input" type="checkbox" id="switch-${diaChi.id}" role="switch"
                                        ${diaChi.trangThai === 'Mặc Định' ? 'checked' : ''}
                                        onchange="updateTrangThai(${diaChi.id}, this.checked)">
                                <label class="custom-control-label" for="switch-${diaChi.id}"></label>
                            </div>
                    </div>

                    <!-- Các nút Sửa và Xóa -->
                    <div class="d-flex gap-2">
                        <button class="btn btn-primary" onclick="editDiaChi(${diaChi.id}, '${diaChi.diaChiCuThe}', '${diaChi.phuong}', '${diaChi.huyen}', '${diaChi.thanhPho}')">Sửa</button>
                        <button class="btn btn-danger" onclick="deleteDiaChi(${diaChi.id})">Xóa</button>
                    </div>
                </div>
            </div>
        `;
        container.appendChild(card);
    });
}

// Mở modal thêm địa chỉ (thiết lập chế độ thêm mới)
function openAddDiaChiModal() {
    document.getElementById("modalDiaChiLabel").textContent = "Thêm Địa Chỉ";
    document.getElementById("diaChiCuThe").value = "";
    document.getElementById("phuong").value = "";
    document.getElementById("huyen").value = "";
    document.getElementById("thanhPho").value = "";
    document.getElementById("idDiaChi").value = "";

    document.getElementById("thanhPhoHidden").value = "";
    document.getElementById("huyenHidden").value = "";
    document.getElementById("phuongHidden").value = "";


    // Ẩn nút "Cập Nhật" và hiển thị nút "Lưu"
    document.getElementById("saveDiaChiButton").classList.remove("d-none");
    document.getElementById("updateDiaChiButton").classList.add("d-none");

    // Mở modal
    new bootstrap.Modal(document.getElementById("modalDiaChi")).show();
}

function updateTrangThai(id, isChecked) {
    fetch(`http://localhost:8080/api/khach-hang/dia-chi/${id}/trang-thai`, {
        method: "PUT",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({trangThai: isChecked ? "Mặc Định" : "Không Mặc Định"})
    })
        .then(response => response.text()) // Đọc dữ liệu dưới dạng text
        .then(text => {
            console.log("Server Response:", text); // Kiểm tra phản hồi từ server
            if (text.includes("thành công")) { // Kiểm tra nếu phản hồi có chứa "thành công"
                Swal.fire({
                    icon: "success",
                    title: "Thành công!",
                    text: "Cập nhật trạng thái địa chỉ thành công.",
                    timer: 1500,
                    showConfirmButton: false
                });
                location.reload(); // Reload lại trang hiện tại
            } else {
                throw new Error("Phản hồi không hợp lệ!");
            }
        })
        .catch(error => {
            console.error('Lỗi cập nhật trạng thái:', error);
            Swal.fire({
                icon: "error",
                title: "Lỗi!",
                text: "Đã xảy ra lỗi trong quá trình cập nhật.",
            });
        });
}

// Thêm mới địa chỉ
function saveDiaChi() {
    const diaChiCuThe = document.getElementById("diaChiCuThe").value;
    const thanhPho = document.getElementById("thanhPho").value;
    const huyen = document.getElementById("huyen").value;
    const phuong = document.getElementById("phuong").value;

    const diaChi = {
        diaChiCuThe,
        thanhPho,
        huyen,
        phuong
    };

    fetch("/api/dia-chi", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(diaChi),
        credentials: "include"  // Gửi session/cookie
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                Swal.fire({
                    icon: "success",
                    title: "Thành công!",
                    text: "Địa chỉ đã được thêm thành công!",
                    timer: 3000,
                    showConfirmButton: false
                });
                location.reload(); // Reload lại trang hiện tại
            } else {
                Swal.fire({
                    icon: "error",
                    title: "Lỗi!",
                    text: data.message || "Có lỗi xảy ra!"
                });
            }
        })
        .catch(error => {
            console.error("Lỗi khi thêm địa chỉ:", error);
            Swal.fire({
                icon: "error",
                title: "Lỗi!",
                text: "Có lỗi xảy ra!"
            });
        });
}

// Mở modal chỉnh sửa địa chỉ
function editDiaChi(idDiaChi) {
    console.log("ID Địa Chỉ cần sửa:", idDiaChi);  // Kiểm tra ID trước khi gửi API
    fetch(`http://localhost:8080/api/dia-chi/${idDiaChi}`)
        .then(response => response.json())
        .then(data => {
            console.log("Dữ liệu nhận được:", data);

            document.getElementById("modalDiaChiLabel").textContent = "Chỉnh Sửa Địa Chỉ";
            document.getElementById("diaChiCuThe").value = data.diaChiCuThe || "";
            document.getElementById("thanhPho").value = data.thanhPho || "";
            document.getElementById("idDiaChi").value = idDiaChi;

            // Gán giá trị cho select huyện
            const huyenSelect = document.getElementById("huyen");
            huyenSelect.innerHTML = ""; // Xóa các option cũ
            if (data.huyen) {
                const huyenOption = new Option(data.huyen, data.huyen);
                huyenSelect.add(huyenOption);
                huyenSelect.value = data.huyen;
            }

            // Gán giá trị cho select phường
            const phuongSelect = document.getElementById("phuong");
            phuongSelect.innerHTML = ""; // Xóa các option cũ
            if (data.phuong) {
                const phuongOption = new Option(data.phuong, data.phuong);
                phuongSelect.add(phuongOption);
                phuongSelect.value = data.phuong;
            }

            // Kiểm tra và gán giá trị khách hàng ID nếu có
            if (data.khachHangId) {
                document.getElementById("khachHangId").value = data.khachHangId;
            }

            // Hiển thị nút "Cập Nhật", ẩn nút "Lưu"
            document.getElementById("saveDiaChiButton").classList.add("d-none");
            document.getElementById("updateDiaChiButton").classList.remove("d-none");

            // Hiển thị modal
            new bootstrap.Modal(document.getElementById("modalDiaChi")).show();
        })
        .catch(error => console.error("Lỗi khi tải dữ liệu địa chỉ:", error));
}

// Cập nhật địa chỉ
function updateDiaChi() {
    const idDiaChi = document.getElementById("idDiaChi").value;
    console.log("ID gửi lên API cập nhật:", idDiaChi); // Kiểm tra ID trước khi gửi

    const diaChi = {
        diaChiCuThe: document.getElementById("diaChiCuThe").value,
        phuong: document.getElementById("phuong").value,
        huyen: document.getElementById("huyen").value,
        thanhPho: document.getElementById("thanhPho").value
    };

    // Gửi yêu cầu PUT đến API với đúng đường dẫn
    fetch(`http://localhost:8080/api/dia-chi/client/${idDiaChi}`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(diaChi),
        credentials: "include"  // Gửi session/cookie
    })
    .then(response => response.json())
        .then(data => {
            if (data.success) {
                Swal.fire({
                    icon: "success",
                    title: "Thành công!",
                    text: data.message || "Địa chỉ đã được sửa thành công!",
                    timer: 3000, // Thời gian hiển thị thông báo là 3 giây
                    showConfirmButton: false // Ẩn nút confirm
                }).then(() => {
                    location.reload(); // Reload lại trang sau khi thông báo hiển thị xong
                });
            } else {
                Swal.fire({
                    icon: "error",
                    title: "Lỗi!",
                    text: data.message || "Có lỗi xảy ra!"
                });
            }
        })
        .catch(error => {
            console.error("Lỗi khi cập nhật địa chỉ:", error);
            Swal.fire({
                icon: "error",
                title: "Lỗi!",
                text: "Có lỗi xảy ra!"
            });
        });
}

function deleteDiaChi(id) {
    Swal.fire({
        title: "Bạn có chắc chắn?",
        text: "Bạn có muốn xóa địa chỉ này không?",
        icon: "warning",
        showCancelButton: true,
        confirmButtonText: "Có, xóa ngay!",
        cancelButtonText: "Hủy",
    }).then((result) => {
        if (result.isConfirmed) {
            fetch(`http://localhost:8080/api/dia-chi/${id}`, {
                method: "DELETE",
            })
                .then(response => response.text()) // Lấy phản hồi dạng text
                .then(text => {
                    try {
                        const data = JSON.parse(text); // Thử chuyển đổi thành JSON
                        console.log("Phản hồi từ server:", data);
                        Swal.fire({
                            icon: "success",
                            title: "Thành công!",
                            text: "Đã xóa địa chỉ.",
                            timer: 3000, // Thời gian hiển thị thông báo là 3 giây
                            showConfirmButton: false // Ẩn nút confirm
                        }).then(() => {
                            location.reload(); // Reload lại trang sau khi thông báo hiển thị xong
                        });
                    } catch (error) {
                        console.log("Phản hồi không phải JSON:", text);
                        Swal.fire({
                            icon: "success",
                            title: "Thành công!",
                            text: text,
                            timer: 3000, // Thời gian hiển thị thông báo là 3 giây
                            showConfirmButton: false // Ẩn nút confirm
                        }).then(() => {
                            location.reload(); // Reload lại trang sau khi thông báo hiển thị xong
                        });
                    }
                })
                .catch(error => {
                    console.error("Lỗi khi xóa địa chỉ:", error);
                    Swal.fire({
                        icon: "error",
                        title: "Lỗi!",
                        text: "Không thể xóa địa chỉ. Vui lòng thử lại sau.",
                        timer: 3000, // Thời gian hiển thị thông báo là 3 giây
                        showConfirmButton: false // Ẩn nút confirm
                    });
                });
        }
    });
}

document.addEventListener("DOMContentLoaded", function () {
    const thanhPhoSelect = document.getElementById("thanhPho");
    const huyenSelect = document.getElementById("huyen");
    const phuongSelect = document.getElementById("phuong");

    const thanhPhoHidden = document.getElementById("thanhPhoHidden");
    const huyenHidden = document.getElementById("huyenHidden");
    const phuongHidden = document.getElementById("phuongHidden");

    // Fetch Tỉnh/Thành phố
    async function fetchProvinces(selectedValue = "") {
        try {
            let response = await fetch("https://provinces.open-api.vn/api/?depth=1");
            let data = await response.json();

            thanhPhoSelect.innerHTML = '<option value="">Chọn Tỉnh/Thành phố</option>';
            let selectedProvinceCode = "";

            data.forEach(province => {
                const selected = province.name === selectedValue ? "selected" : "";
                if (selected) selectedProvinceCode = province.code;
                thanhPhoSelect.innerHTML += `<option value="${province.name}" data-code="${province.code}" ${selected}>${province.name}</option>`;
            });

            // Nếu có tỉnh được chọn, load danh sách quận/huyện
            if (selectedProvinceCode) {
                fetchDistricts(selectedProvinceCode, huyenHidden.value);
            }
        } catch (error) {
            console.error("Lỗi khi lấy danh sách tỉnh/thành phố:", error);
        }
    }

    // Fetch Quận/Huyện
    async function fetchDistricts(provinceCode, selectedValue = "") {
        try {
            let response = await fetch(`https://provinces.open-api.vn/api/p/${provinceCode}?depth=2`);
            let data = await response.json();

            huyenSelect.innerHTML = '<option value="">Chọn Quận/Huyện</option>';
            let selectedDistrictCode = "";

            data.districts.forEach(district => {
                const selected = district.name === selectedValue ? "selected" : "";
                if (selected) selectedDistrictCode = district.code;
                huyenSelect.innerHTML += `<option value="${district.name}" data-code="${district.code}" ${selected}>${district.name}</option>`;
            });

            // Nếu có quận được chọn, load danh sách phường/xã
            if (selectedDistrictCode) {
                fetchWards(selectedDistrictCode, phuongHidden.value);
            }
        } catch (error) {
            console.error("Lỗi khi lấy danh sách quận/huyện:", error);
        }
    }

    // Fetch Phường/Xã
    async function fetchWards(districtCode, selectedValue = "") {
        try {
            let response = await fetch(`https://provinces.open-api.vn/api/d/${districtCode}?depth=2`);
            let data = await response.json();

            phuongSelect.innerHTML = '<option value="">Chọn Phường/Xã</option>';

            data.wards.forEach(ward => {
                const selected = ward.name === selectedValue ? "selected" : "";
                phuongSelect.innerHTML += `<option value="${ward.name}" data-code="${ward.code}" ${selected}>${ward.name}</option>`;
            });
        } catch (error) {
            console.error("Lỗi khi lấy danh sách phường/xã:", error);
        }
    }

    // Gọi hàm fetchProvinces khi load trang
    fetchProvinces();

    // Khi Tỉnh/Thành phố thay đổi, gọi lại fetchDistricts
    thanhPhoSelect.addEventListener("change", function () {
        const provinceCode = this.selectedOptions[0].dataset.code;
        fetchDistricts(provinceCode);
    });

    // Khi Quận/Huyện thay đổi, gọi lại fetchWards
    huyenSelect.addEventListener("change", function () {
        const districtCode = this.selectedOptions[0].dataset.code;
        fetchWards(districtCode);
    });

    // Khi Phường/Xã thay đổi, lưu lại giá trị vào hidden field
    phuongSelect.addEventListener("change", function () {
        phuongHidden.value = this.value;
    });
});

