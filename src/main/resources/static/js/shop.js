async function createHoaDon() {
    try {
        const response = await fetch(`/create`, {
            method: 'POST',
            credentials: 'include' // Quan trọng để gửi session cookie
        });

        const result = await response.json();

        if (!response.ok) {
            throw new Error(result.message || 'Lỗi khi tạo hóa đơn');
        }

        // Hiển thị thông báo thành công
        Swal.fire({
            title: 'Tạo hóa đơn thành công!',
            text: `Mã hóa đơn: ${result.maHoaDon}`,
            icon: 'success',
            confirmButtonText: 'OK'
        });

        addHoaDonToUI(result);
    } catch (error) {
        console.error('Lỗi:', error);

        // Hiển thị thông báo lỗi
        Swal.fire({
            title: 'Lỗi!',
            text: error.message,
            icon: 'error',
            confirmButtonText: 'Đóng'
        });
    }
}

function addHoaDonToUI(hoaDon) {
    const container = document.querySelector(".row.g-4");

    const newCard = document.createElement("div");
    newCard.className = "col-12 col-sm-6 col-lg-4";
    newCard.innerHTML = `
        <div class="card border-primary shadow-sm position-relative rounded-3 overflow-hidden">
            <div class="position-absolute top-0 end-0 m-2">
                <button type="button" class="btn btn-sm btn-danger"
                        onclick="confirmDeleteHoaDon(this, '${hoaDon.id}')">
                    <i class="fas fa-times"></i>
                </button>
            </div>
            <form action="/admin/sell-counter" method="GET">
                <input type="hidden" name="hoaDonId" value="${hoaDon.id}">
                <button type="submit" class="btn p-0 w-100">
                    <div class="card-header bg-primary text-white text-center py-3">
                        <h6># ${hoaDon.maHoaDon}</h6>
                    </div>
                    <div class="card-body text-center">
                        <p class="card-text text-muted">
                            <i class="fas fa-shopping-cart"></i> Số lượng Sản Phẩm:
                            <strong th:text="${hoaDon.tongSanPham != null ? hoaDon.tongSanPham : 0}"></strong>
                        </p>
                    </div>
                    <div class="card-footer bg-light text-center">
                        <small class="text-muted">Chi tiết hóa đơn</small>
                    </div>
                </button>
            </form>
        </div>
    `;

    container.prepend(newCard);
}

<!--API Delete Hóa Đơn-->
function confirmDeleteHoaDon(button, hoaDonId) {
    Swal.fire({
        title: "Xác nhận",
        text: "Bạn có chắc chắn muốn xóa hóa đơn này không?",
        icon: "warning",
        showCancelButton: true,
        confirmButtonText: "Có, xóa ngay!",
        cancelButtonText: "Hủy",
        reverseButtons: true
    }).then(result => {
        if (result.isConfirmed) {
            deleteHoaDon(button, hoaDonId);
        }
    });
}
function deleteHoaDon(button, hoaDonId) {
    fetch(`/delete/${hoaDonId}`, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'}
    })
        .then(response => response.json())
        .then(data => {
            if (data.message) {
                Swal.fire({
                    title: "Xóa thành công!",
                    text: data.message,
                    icon: "success",
                    timer: 1500,
                    showConfirmButton: false
                });

                setTimeout(() => {
                    const card = button.closest(".col-12.col-sm-6.col-lg-4");
                    if (card) card.remove();
                    window.location.href = "/admin/sell-counter";
                }, 1500);
            }
        })
        .catch(() => {
            Swal.fire({
                title: "Lỗi!",
                text: "Không thể xóa hóa đơn.",
                icon: "error"
            });
        });
}

<!--API Thêm Khách Hàng-->
function addCustomerToInvoice(form) {
    const hoaDonId = form.querySelector('input[name="hoaDonId"]').value;
    const khachHangId = form.querySelector('input[name="khachHangId"]').value;

    fetch('/add-customer', {
        method: 'POST',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        body: new URLSearchParams({hoaDonId, khachHangId})
    })
        .then(response => response.json())
        .then(data => {
            setTimeout(() => {
                if (data.message) {
                    Swal.fire({
                        title: "Thành công!",
                        text: data.message,
                        icon: "success",
                        timer: 1500,
                        showConfirmButton: false
                    }).then(() => location.reload());
                } else {
                    Swal.fire({
                        title: "Lỗi!",
                        text: data.error || "Có lỗi xảy ra khi thêm khách hàng!",
                        icon: "error",
                        timer: 1500,
                        showConfirmButton: false
                    });
                }
            }, 500);
        })
        .catch(error => {
            console.error('Lỗi:', error);
            setTimeout(() => {
                Swal.fire({
                    title: "Lỗi!",
                    text: "Có lỗi xảy ra khi thêm khách hàng!",
                    icon: "error",
                    timer: 1500,
                    showConfirmButton: false
                });
            }, 500);
        });
}

<!--API Xóa Khách Hàng-->
function deleteCustomerFromInvoice(button) {
    const hoaDonId = button.getAttribute("data-hoaDonId");

    if (!hoaDonId || isNaN(hoaDonId)) {
        Swal.fire({
            title: "Lỗi!",
            text: "ID hóa đơn không hợp lệ!",
            icon: "error",
            timer: 1500,
            showConfirmButton: false
        });
        return;
    }

    Swal.fire({
        title: "Bạn có chắc chắn?",
        text: "Khách hàng sẽ bị xóa khỏi hóa đơn!",
        icon: "warning",
        showCancelButton: true,
        confirmButtonColor: "#d33",
        cancelButtonColor: "#3085d6",
        confirmButtonText: "Xóa",
        cancelButtonText: "Hủy"
    }).then((result) => {
        if (result.isConfirmed) {
            fetch('/delete-customer', {
                method: 'POST',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                body: new URLSearchParams({hoaDonId})
            })
                .then(response => response.json())
                .then(data => {
                    setTimeout(() => {
                        if (data.message) {
                            Swal.fire({
                                title: "Thành công!",
                                text: data.message,
                                icon: "success",
                                timer: 1500,
                                showConfirmButton: false
                            }).then(() => location.reload());
                        } else {
                            Swal.fire({
                                title: "Lỗi!",
                                text: data.error || "Lỗi khi xóa khách hàng!",
                                icon: "error",
                                timer: 1500,
                                showConfirmButton: false
                            });
                        }
                    }, 500);
                })
                .catch(error => {
                    console.error('Lỗi:', error);
                    setTimeout(() => {
                        Swal.fire({
                            title: "Lỗi!",
                            text: "Lỗi khi xóa khách hàng!",
                            icon: "error",
                            timer: 1500,
                            showConfirmButton: false
                        });
                    }, 500);
                });
        }
    });
}

<!--API Phương Thức Thanh Toán-->
$(document).ready(function () {
    const paymentOptions = $("input[name='phuongThucThanhToanId']");
    const paymentDetails = $("#paymentDetails");
    const paymentMethodText = $("#paymentMethodText");
    const orderInfo = $("#orderInfo");

    function parseMoney(value) {
        return parseFloat((value || "0").toString().replace(/,/g, "")) || 0;
    }

    const tongTienHoaDon = parseMoney(orderInfo.data("tongtien"));
    const phiShip = parseMoney(orderInfo.data("phiship"));
    const maGiamGia = parseMoney(orderInfo.data("magiamgia"));
    const tongThanhToan = tongTienHoaDon + phiShip - maGiamGia;

    paymentOptions.on("change", function () {
        const selectedText = $(`label[for="${this.id}"]`).text().trim();
        paymentDetails.empty();

        if (selectedText === "Tiền Mặt") {
            paymentDetails.html(`
                <div class="mb-3">
                    <label for="cashAmount" class="form-label">
                        <i class="fas fa-money-bill-wave"></i> Nhập số tiền khách đưa:
                    </label>
                    <input type="number" id="cashAmount" class="form-control" placeholder="Nhập số tiền khách đưa" min="0">
                </div>
                <div class="mb-3">
                    <label class="form-label">
                        <i class="fas fa-coins"></i> Số tiền cần thanh toán:
                    </label>
                    <input type="text" class="form-control" value="${tongTienHoaDon.toLocaleString()} VND" readonly>
                </div>
                <div class="mb-3">
                    <label class="form-label">
                        <i class="fas fa-money-check-alt"></i> Số tiền thối lại:
                    </label>
                    <input type="text" id="changeAmount" class="form-control" readonly>
                </div>
            `);

            // Tự động điền số tiền khách đưa = số tiền cần thanh toán
            $("#cashAmount").val(tongTienHoaDon);

            // Tính số tiền thối lại khi bắt đầu
            const change = tongTienHoaDon - tongTienHoaDon;
            $("#changeAmount").val(`${change.toLocaleString()} VND`).css("color", change < 0 ? "red" : "green");

            // Kiểm tra và tính số tiền thối lại khi người dùng nhập
            $("#cashAmount").on("input", function () {
                const cashGiven = parseMoney($(this).val());

                // Nếu số tiền khách đưa nhỏ hơn số tiền cần thanh toán, không cho phép cập nhật
                if (cashGiven < tongTienHoaDon) {
                    $("#changeAmount").val(`Thiếu ${Math.abs(cashGiven - tongTienHoaDon).toLocaleString()} VND`).css("color", "red");
                    $("#addPaymentMethodButton").prop("disabled", true);  // Vô hiệu hóa nút Cập nhật
                    return;
                }

                // Tính số tiền thối lại
                const change = cashGiven - tongTienHoaDon;

                // Cập nhật số tiền thối lại
                $("#changeAmount").val(
                    change < 0
                        ? `Thiếu ${Math.abs(change).toLocaleString()} VND`
                        : `${change.toLocaleString()} VND`
                ).css("color", change < 0 ? "red" : "green");

                // Kích hoạt lại nút Cập nhật nếu số tiền khách đưa đủ
                $("#addPaymentMethodButton").prop("disabled", false);
            });
        } else if (selectedText === "Chuyển Khoản") {
            paymentDetails.html(`
                <div class="mb-3 text-center">
                    <img id="qrCodeImage" src="https://api.qrserver.com/v1/create-qr-code/?size=250x250&data=Demo+Thanh+Toan+Ngan+Hang" alt="QR Code" class="img-fluid" style="max-width: 250px;">
                </div>
            `);

             // Kích hoạt lại nút Cập nhật nếu số tiền khách đưa đủ
             $("#addPaymentMethodButton").prop("disabled", false);
        }
    });

    // Kiểm tra lại số tiền cần thanh toán khi quay lại phương thức thanh toán tiền mặt
    paymentOptions.filter(":checked").trigger("change");

    $("#addPaymentMethodButton").on("click", function () {
        const formData = $("#paymentMethodForm").serialize();
        const selectedPayment = $("input[name='phuongThucThanhToanId']:checked").val();
        const cashGiven = parseMoney($("#cashAmount").val());

        // Kiểm tra nếu số tiền khách đưa nhỏ hơn số tiền cần thanh toán
        if (selectedPayment === "Tiền Mặt" && cashGiven < tongTienHoaDon) {
            Swal.fire({
                title: "Lỗi!",
                text: "Số tiền khách đưa không đủ để thanh toán!",
                icon: "error",
                timer: 1500,
                showConfirmButton: false
            });
            return;  // Dừng lại và không gửi yêu cầu
        }

        if (!selectedPayment) {
            Swal.fire({
                title: "Lỗi!",
                text: "Vui lòng chọn phương thức thanh toán!",
                icon: "error",
                timer: 1500,
                showConfirmButton: false
            });
            return;
        }

        // Gửi dữ liệu đi nếu mọi thứ đúng
        $.ajax({
            url: '/add-payment-method',
            method: 'POST',
            data: formData,
            beforeSend: function () {
                $("#addPaymentMethodButton").prop("disabled", true);
            },
            success: function (response) {
                setTimeout(() => {
                    if (response.success) {
                        paymentMethodText.text(response.updatedPaymentMethod);
                        $("#paymentModal").modal("hide");

                        Swal.fire({
                            title: "Thành công!",
                            text: "Cập nhật phương thức thanh toán thành công!",
                            icon: "success",
                            timer: 1500,
                            showConfirmButton: false
                        }).then(() => location.reload());
                    } else {
                        Swal.fire({
                            title: "Lỗi!",
                            text: response.message || "Cập nhật không thành công!",
                            icon: "error",
                            timer: 1500,
                            showConfirmButton: false
                        });
                    }
                }, 400);
            },
            error: function (xhr) {
                setTimeout(() => {
                    Swal.fire({
                        title: "Lỗi!",
                        text: "Lỗi: " + xhr.responseText,
                        icon: "error",
                        timer: 1500,
                        showConfirmButton: false
                    });
                }, 400);
            },
            complete: function () {
                $("#addPaymentMethodButton").prop("disabled", false);
            }
        });
    });
});

<!--API Thay Đổi Loại Giao Hàng và Trả Sau-->
document.addEventListener("DOMContentLoaded", () => {
    const deliverySwitch = document.querySelector("#deliverySwitch");
    const payLaterSwitch = document.querySelector("#payLaterSwitch");

    const toggleSections = (isChecked) => {
        document.getElementById("shippingForm").style.display = isChecked ? "block" : "none";
        document.getElementById("payLaterSection").style.display = isChecked ? "block" : "none";
    };

    if (deliverySwitch) {
        toggleSections(deliverySwitch.checked);
        deliverySwitch.addEventListener("change", () => toggleSections(deliverySwitch.checked));
    }

    if (payLaterSwitch) {
        payLaterSwitch.addEventListener("change", () => {
            document.getElementById("payLaterSection").style.display = payLaterSwitch.checked ? "block" : "none";
        });
    }

    document.querySelectorAll(".form-check-input").forEach(checkbox => {
        checkbox.addEventListener("change", function () {
            const hoaDonId = this.getAttribute("data-hoa-don-id");
            const apiUrl = this.id === "deliverySwitch"
                ? `/api/hoa-don/toggle-delivery/${hoaDonId}`
                : `/cap-nhat-loai-giao-dich/${hoaDonId}`;

            fetch(apiUrl, {method: "PUT", headers: {"Content-Type": "application/json"}})
                .then(res => res.json())
                .then(data => {
                    if (data.successMessage) {
                        Swal.fire({
                            icon: "success",
                            title: "Cập nhật thành công!",
                            timer: 1500,
                            showConfirmButton: false
                        }).then(() => location.reload());
                    } else {
                        Swal.fire({
                            icon: "error",
                            title: "Lỗi!",
                            text: data.errorMessage || "Cập nhật thất bại!",
                            timer: 2000,
                            showConfirmButton: false
                        });
                        this.checked = !this.checked;
                    }
                })
                .catch(() => Swal.fire({
                    icon: "error",
                    title: "Lỗi kết nối!",
                    text: "Không thể kết nối server!",
                    timer: 2000,
                    showConfirmButton: false
                }));
        });
    });
});

<!--API Phiếu Giảm Giá-->
function applyPhieuGiamGia(button) {
    const hoaDonId = document.getElementById("hoaDonId").value;
    const tenPhieuGiamGia = document.getElementById("tenPhieuGiamGia").value || ""; // Cho phép bỏ chọn

    if (!hoaDonId || isNaN(hoaDonId)) {
        Swal.fire("Lỗi!", "ID hóa đơn không hợp lệ!", "error");
        return;
    }

    button.disabled = true;
    button.innerText = "Đang áp dụng...";

    fetch('/apply-phieu-giam-gia', {
        method: 'POST',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        body: new URLSearchParams({hoaDonId, tenPhieuGiamGia})
    })
        .then(res => res.json())
        .then(data => {
            Swal.fire({
                title: data.success ? "Thành công!" : "Lỗi!",
                text: data.success ? data.message : (data.error || "Lỗi khi áp dụng phiếu giảm giá!"),
                icon: data.success ? "success" : "error"
            }).then(() => {
                if (data.success) {
                    bootstrap.Modal.getInstance(document.getElementById("discountModal")).hide();
                    setTimeout(() => location.reload(), 500);
                }
            });
        })
        .catch(() => {
            Swal.fire("Lỗi!", "Lỗi hệ thống, vui lòng thử lại sau!", "error");
        })
        .finally(() => {
            button.disabled = false;
            button.innerText = "Áp dụng";
        });
}

<!--API Update Địa Chỉ Giao Hàng-->
document.addEventListener("DOMContentLoaded", function () {
    const form = document.querySelector("#updateShippingForm");

    if (!form) {
        console.warn("Không tìm thấy form #updateShippingForm");
        return;
    }

    form.addEventListener("submit", function (event) {
        event.preventDefault(); // Ngăn chặn gửi form mặc định

        const formData = new FormData(form);
        const hoaDonId = formData.get("hoaDonId");
        const nguoiNhan = formData.get("nguoiNhan").trim();
        const soDienThoai = formData.get("soDienThoai").trim();
        const diaChiCuThe = formData.get("diaChiCuThe").trim();
        const thanhPho = formData.get("thanhPho").trim();
        const huyen = formData.get("huyen").trim();
        const phuong = formData.get("phuong").trim();

        // Xóa thông báo lỗi trước khi kiểm tra
        document.getElementById("shippingNguoiNhanError").textContent = "";
        document.getElementById("shippingSoDienThoaiError").textContent = "";
        document.getElementById("diaChiCuTheError").textContent = "";
        document.getElementById("thanhPhoError").textContent = "";
        document.getElementById("huyenError").textContent = "";
        document.getElementById("phuongError").textContent = "";

        let isValid = true;

        // Kiểm tra ID hóa đơn hợp lệ
        if (!hoaDonId || isNaN(hoaDonId)) {
            Swal.fire("Lỗi!", "ID hóa đơn không hợp lệ!", "error");
            return;
        }

        // Kiểm tra họ tên
        const nameRegex = /^[A-Za-zÀ-ỹ\s]+$/;
        const words = nguoiNhan.split(/\s+/).filter(word => word.length > 0);

        if (!nguoiNhan) {
            document.getElementById("shippingNguoiNhanError").textContent = "Họ tên không được để trống!";
            isValid = false;
        } else if (!nameRegex.test(nguoiNhan)) {
            document.getElementById("shippingNguoiNhanError").textContent = "Họ tên chỉ được chứa chữ cái và khoảng trắng!";
            isValid = false;
        } else if (words.length < 2) {
            document.getElementById("shippingNguoiNhanError").textContent = "Họ tên phải có ít nhất 2 từ!";
            isValid = false;
        }

        // Kiểm tra số điện thoại (bắt đầu bằng 0, 10-11 số)
        const phoneRegex = /^(0\d{9,10})$/;
        if (!soDienThoai) {
            document.getElementById("shippingSoDienThoaiError").textContent = "Số điện thoại không được để trống!";
            isValid = false;
        } else if (!phoneRegex.test(soDienThoai)) {
            document.getElementById("shippingSoDienThoaiError").textContent = "Số điện thoại không hợp lệ!";
            isValid = false;
        }

        // Kiểm tra địa chỉ

        if (!diaChiCuThe) {
            document.getElementById("diaChiCuTheError").textContent = "Đia chỉ cụ thể không được để trống!";
            isValid = false;
        }
        if (!thanhPho) {
            document.getElementById("thanhPhoError").textContent = "Vui lòng chọn Tỉnh/Thành phố!";
            isValid = false;
        }
        if (!huyen) {
            document.getElementById("huyenError").textContent = "Vui lòng chọn Quận/Huyện!";
            isValid = false;
        }
        if (!phuong) {
            document.getElementById("phuongError").textContent = "Vui lòng chọn Phường/Xã!";
            isValid = false;
        }

        if (!isValid) return; // Dừng nếu có lỗi

        // Gửi yêu cầu cập nhật nếu dữ liệu hợp lệ
        fetch("/update-shipping", {
            method: "PUT",
            headers: {"Content-Type": "application/x-www-form-urlencoded"},
            body: new URLSearchParams([...formData])
        })
            .then(response => response.json())
            .then(data => {
                Swal.fire({
                    title: data.message ? "Thành công!" : "Lỗi!",
                    text: data.message || data.error || "Lỗi khi cập nhật địa chỉ giao hàng!",
                    icon: data.message ? "success" : "error"
                }).then(() => {
                    if (data.message) {
                        setTimeout(() => location.reload(), 500);
                    }
                });
            })
            .catch(() => {
                Swal.fire("Lỗi!", "Không thể cập nhật địa chỉ giao hàng!", "error");
            });
    });
});

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
    fetchProvinces(thanhPhoHidden.value);

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
<!-- API Update New Khách Hàng -->
document.addEventListener("DOMContentLoaded", function () {
    const form = document.querySelector("#addCustomerForm");

    if (!form) return;

    form.addEventListener("submit", function (event) {
        event.preventDefault();

        const formData = new FormData(form);
        const hoaDonId = formData.get("hoaDonId");
        const nguoiNhan = formData.get("nguoiNhan").trim();
        const soDienThoai = formData.get("soDienThoai").trim();

        // Reset lỗi
        document.getElementById("nguoiNhanError").textContent = "";
        document.getElementById("soDienThoaiError").textContent = "";

        let isValid = true;

        // Validate ID hóa đơn
        if (!hoaDonId || isNaN(hoaDonId)) {
            Swal.fire("Lỗi!", "ID hóa đơn không hợp lệ!", "error");
            return;
        }

        // Validate họ tên
        const nameRegex = /^[A-Za-zÀ-ỹ\s]+$/;
        const words = nguoiNhan.split(/\s+/).filter(word => word.length > 0);

        if (!nguoiNhan) {
            document.getElementById("nguoiNhanError").textContent = "Họ tên không được để trống!";
            isValid = false;
        } else if (!nameRegex.test(nguoiNhan)) {
            document.getElementById("nguoiNhanError").textContent = "Họ tên chỉ được chứa chữ cái và khoảng trắng!";
            isValid = false;
        } else if (words.length < 2) {
            document.getElementById("nguoiNhanError").textContent = "Họ tên phải có ít nhất 2 từ!";
            isValid = false;
        }

        // Validate số điện thoại
        const phoneRegex = /^(0\d{9,10})$/;
        if (!soDienThoai) {
            document.getElementById("soDienThoaiError").textContent = "Số điện thoại không được để trống!";
            isValid = false;
        } else if (!phoneRegex.test(soDienThoai)) {
            document.getElementById("soDienThoaiError").textContent = "Số điện thoại không hợp lệ!";
            isValid = false;
        }

        if (!isValid) return;

        // Gửi API
        fetch("/add-new-customer", {
            method: "POST",
            body: formData,
        })
            .then(response => response.json().then(data => {
                if (!response.ok) throw new Error(data.message || "Lỗi từ server");
                return data;
            }))
            .then(data => {
                const status = data.status || "info";
                const titleMap = {
                    success: "Thành công!",
                    warning: "Cảnh báo!",
                    error: "Lỗi!",
                    info: "Thông báo"
                };

                Swal.fire({
                    title: titleMap[status] || "Thông báo",
                    text: data.message || "Có lỗi xảy ra!",
                    icon: status
                }).then(() => {
                    if (status === "success") {
                        form.reset(); // Reset form
                        setTimeout(() => location.reload(), 500);
                    }
                });
            })
            .catch(error => {
                console.error("Lỗi:", error);
                Swal.fire("Lỗi!", error.message || "Lỗi khi cập nhật thông tin khách hàng!", "error");
            });
    });
});

// Lưu hoaDonId vào sessionStorage khi chọn hóa đơn hoặc từ URL
function saveHoaDonIdFromURL() {
    const urlParams = new URLSearchParams(window.location.search);
    const hoaDonId = urlParams.get('hoaDonId');

    if (hoaDonId && !sessionStorage.getItem('selectedHoaDonId')) {
        localStorage.setItem('selectedHoaDonId', hoaDonId);
        console.log("HoaDonId từ URL đã lưu:", hoaDonId);
    }
}

// Lưu hoaDonId vào sessionStorage khi chọn hóa đơn
function saveHoaDonId(hoaDonId) {
    localStorage.setItem('selectedHoaDonId', hoaDonId);
    console.log("HoaDonId đã lưu:", hoaDonId);
}

// API Thêm Sản Phẩm
function addProductOrder(button) {
    const row = button.closest('tr');
    const sanPhamChiTietId = parseInt(row.querySelector('td:first-child')?.textContent.trim(), 10);
    const soLuong = parseInt(row.querySelector('input[name="soLuong"]')?.value, 10);
    let hoaDonId = parseInt(row.querySelector('input[name="hoaDonId"]')?.value, 10);

    if (!hoaDonId || isNaN(hoaDonId)) {
        const urlParams = new URLSearchParams(window.location.search);
        hoaDonId = urlParams.get('hoaDonId');

        if (!hoaDonId || isNaN(hoaDonId)) {
            Swal.fire({ title: "Lỗi!", text: "Không tìm thấy hóa đơn hợp lệ!", icon: "error" });
            return;
        }
    }

    hoaDonId = parseInt(hoaDonId, 10);
    console.log("Gửi yêu cầu thêm sản phẩm:", { sanPhamChiTietId, hoaDonId, soLuong });

    fetch('/add-product-order', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ sanPhamChiTietId, hoaDonId, soLuong })
    })
    .then(response => {
        if (!response.ok) {
            return response.json().then(error => {
                throw new Error(error.errorMessage || "Có lỗi xảy ra từ server!");
            });
        }
        return response.json();
    })
    .then(response => {
        handleResponse(response, hoaDonId, sanPhamChiTietId)
        setTimeout(() => {
            location.reload();
        }, 1500); // Reload sau 3 giây
    })
    .catch(error => {
        console.error("Lỗi khi gửi yêu cầu:", error);

        Swal.fire({
            title: "Lỗi!",
            text: error.message || "Có lỗi xảy ra!",
            icon: "error",
            showConfirmButton: false,
            timer: 1500
        });

        setTimeout(() => {
            location.reload();
        }, 1500);
    });
}

// API Cập Nhật Số Lượng
function updateQuantityOrder(input) {
    const row = input.closest('tr');
    const hoaDonChiTietId = parseInt(row.querySelector('input[name="hoaDonChiTietId"]').value, 10);
    const sanPhamChiTietId = parseInt(row.dataset.sanPhamChiTietId, 10); // Giữ ID sản phẩm
    const soLuong = parseInt(input.value, 10);
    let hoaDonId = parseInt(row.querySelector('input[name="hoaDonId"]')?.value, 10);

    if (isNaN(hoaDonChiTietId) || isNaN(hoaDonId) || isNaN(soLuong) || soLuong < 1) {
        Swal.fire({ title: "Lỗi!", text: "Thông tin không hợp lệ!", icon: "error" });
        return;
    }

    hoaDonId = parseInt(hoaDonId, 10);
    const data = { hoaDonChiTietId, hoaDonId, soLuong };

    Swal.fire({
        title: "Đang cập nhật...",
        text: "Vui lòng đợi trong giây lát.",
        icon: "info",
        showConfirmButton: false,
        allowOutsideClick: false
    });

    fetch('/update-product-order', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    })
    .then(response => response.json())
    .then(response => {
        handleResponse(response, hoaDonId, sanPhamChiTietId);
        setTimeout(() => {
            location.reload();
        }, 1500); // Reload sau 3 giây
    })
    .catch(error => {
        console.error("Lỗi khi gửi yêu cầu:", error);

        Swal.fire({
            title: "Lỗi!",
            text: error.message || "Có lỗi xảy ra!",
            icon: "error",
            showConfirmButton: false,
            timer: 1500
        });

        setTimeout(() => {
            location.reload();
        }, 1500);
    });
}

// API Xóa Sản Phẩm
function deleteProductOrder(button) {
    const row = button.closest('tr');
    const hoaDonChiTietId = parseInt(row.querySelector('input[name="hoaDonChiTietId"]').value, 10);
    const sanPhamChiTietId = parseInt(row.dataset.sanPhamChiTietId, 10);
    let hoaDonId = parseInt(row.querySelector('input[name="hoaDonId"]')?.value, 10);

    if (isNaN(hoaDonChiTietId) || isNaN(hoaDonId)) {
        Swal.fire({ title: "Lỗi!", text: "Thông tin không hợp lệ!", icon: "error" });
        return;
    }

    hoaDonId = parseInt(hoaDonId, 10);

    Swal.fire({
        title: "Xác nhận xóa?",
        text: "Bạn có chắc chắn muốn xóa sản phẩm này khỏi đơn hàng không?",
        icon: "warning",
        showCancelButton: true,
        confirmButtonText: "Xóa",
        cancelButtonText: "Hủy"
    }).then((result) => {
        if (result.isConfirmed) {
            fetch('/delete-product-order', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ hoaDonChiTietId, hoaDonId })
            })
                .then(response => response.json())
                .then(response => handleResponse(response, hoaDonId, sanPhamChiTietId))
                .catch(error => {
                    console.error("Lỗi khi gửi yêu cầu:", error);

                    Swal.fire({
                        title: "Lỗi!",
                        text: error.message || "Có lỗi xảy ra!",
                        icon: "error",
                        showConfirmButton: false,
                        timer: 1500
                    });

                    setTimeout(() => {
                        location.reload();
                    }, 1500);
                });
        }
    });
}

// Xử lý phản hồi chung cho các API
function handleResponse(response, hoaDonId, sanPhamChiTietId) {
    if (response.successMessage) {
        Swal.fire({
            title: "Thành công!",
            text: response.successMessage,
            icon: "success",
            timer: 1500,
            showConfirmButton: false
        }).then(() => {
            // Lưu thông tin trước khi redirect
            sessionStorage.setItem('scrollPosition', window.scrollY);
            sessionStorage.setItem('highlightProductId', sanPhamChiTietId); // Đánh dấu sản phẩm vừa thao tác
            sessionStorage.setItem('previousUrl', window.location.href); // Lưu URL trước khi chuyển trang

            redirectToAppropriatePage(response.trangThai, hoaDonId);
        });
    } else {
        Swal.fire({
            title: "Lỗi!",
            text: response.errorMessage || "Thao tác không thành công!",
            icon: "error"
        });
    }
}

// Xử lý lỗi chung cho các API
function handleError(error) {
    console.error("Lỗi khi gửi yêu cầu:", error);

    sessionStorage.clear();
    localStorage.removeItem('scrollPosition');
    localStorage.removeItem('highlightProductId');
    localStorage.removeItem('previousUrl');

    // Chọn một trong hai cách dưới đây:

    // Cách 1: Tự động tắt sau 1.5s
    Swal.fire({
        title: "Lỗi!",
        text: "Có lỗi xảy ra: " + error.message,
        icon: "error",
        showConfirmButton: false,
        timer: 1500
    }).then(() => location.reload());

    // Hoặc Cách 2: Người dùng ấn OK mới reload
    /*
    Swal.fire({
        title: "Lỗi!",
        text: "Có lỗi xảy ra: " + error.message,
        icon: "error",
        confirmButtonText: "OK"
    }).then(() => location.reload());
    */
}



// 🏷️ Chuyển hướng và lưu vị trí cuộn
function redirectToAppropriatePage(trangThai, hoaDonId) {
    let redirectUrl;

    if (trangThai === "Đang Xử Lý") {
        redirectUrl = `/admin/sell-counter?hoaDonId=${hoaDonId}`;
    } else if (trangThai === "Chờ Xác Nhận") {
        redirectUrl = `/admin/order-details?hoaDonId=${hoaDonId}`;
    } else {
        redirectUrl = `/admin/sell-counter?hoaDonId=${hoaDonId}`;
    }

    const currentUrl = window.location.pathname + window.location.search;

    // Nếu đã ở đúng trang, chỉ cần reload thay vì redirect
    if (currentUrl === redirectUrl) {
        window.location.reload();
    } else {
        // Chuyển hướng sang URL mới
        window.location.href = redirectUrl;
    }
}

// 🎯 Cuộn lại và highlight sản phẩm
document.addEventListener("DOMContentLoaded", async () => {
    await new Promise((resolve) => setTimeout(resolve, 100)); // Đợi 100ms cho DOM load

    // ✅ Khôi phục vị trí cuộn
    const savedScrollPosition = localStorage.getItem('scrollPosition');
    if (savedScrollPosition !== null) {
        requestAnimationFrame(() => {
            window.scrollTo({ top: parseInt(savedScrollPosition, 10), behavior: "smooth" });
            localStorage.removeItem('scrollPosition');
        });
    }

    // ✅ Tạo hiệu ứng highlight sản phẩm
    const highlightProductId = localStorage.getItem('highlightProductId');
    if (highlightProductId) {
        const highlightedRow = document.querySelector(`tr[data-sanphamchitietid="${highlightProductId}"]`);
        if (highlightedRow) {
            // 🕵️‍♂️ Chỉ highlight nếu phần tử thực sự xuất hiện trên màn hình
            const observer = new IntersectionObserver((entries) => {
                entries.forEach((entry) => {
                    if (entry.isIntersecting) {
                        highlightedRow.style.backgroundColor = "#ffff99"; // Màu vàng nhạt
                        setTimeout(() => {
                            highlightedRow.style.transition = "background-color 1s";
                            highlightedRow.style.backgroundColor = "";
                        }, 3000);
                        observer.disconnect(); // Ngừng theo dõi sau khi highlight
                    }
                });
            });

            observer.observe(highlightedRow);
        }
        localStorage.removeItem('highlightProductId');
    }
});


// Đảm bảo khi trang được tải lại, hoaDonId được lấy từ URL nếu chưa có trong sessionStorage
window.onload = function() {
    saveHoaDonIdFromURL();
};

<!-- Script xử lý thanh toán -->
document.addEventListener("DOMContentLoaded", function () {
    const confirmButton = document.getElementById("confirmPayment");
    const paymentForm = document.getElementById("paymentForm");

    if (confirmButton && paymentForm) {
        confirmButton.addEventListener("click", function () {
            paymentForm.submit();
        });
    } else {
        console.warn("Không tìm thấy phần tử 'confirmPayment' hoặc 'paymentForm'");
    }
});

document.addEventListener("DOMContentLoaded", function () {
    let successMessage = document.getElementById("successMessage")?.value || "";
    let errorMessage = document.getElementById("errorMessage")?.value || "";

    if (successMessage.trim()) {
        Swal.fire({
            icon: 'success',
            title: 'Thành công!',
            text: successMessage,
            confirmButtonColor: '#3085d6'
        });
    }

    if (errorMessage.trim()) {
        Swal.fire({
            icon: 'error',
            title: 'Lỗi!',
            text: errorMessage,
            confirmButtonColor: '#d33'
        });
    }
});
// Hàm lấy tham số từ URL
function getQueryParam(param) {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get(param);
}

// Hàm chọn hóa đơn
function selectHoaDon(card) {
    // Xóa class 'selected-hoa-don' từ tất cả hóa đơn khác
    document.querySelectorAll(".card.border-primary").forEach(c => c.classList.remove("selected-hoa-don"));

    // Thêm class 'selected-hoa-don' vào hóa đơn được chọn
    card.classList.add("selected-hoa-don");

    // Lưu ID của hóa đơn được chọn vào localStorage
    const hoaDonId = card.querySelector('input[name="hoaDonId"]').value;
    localStorage.setItem("selectedHoaDonId", hoaDonId);
}

// Khi trang load lại, kiểm tra và giữ trạng thái sáng cho hóa đơn đã chọn
document.addEventListener("DOMContentLoaded", function () {
    const hoaDonIdFromUrl = getQueryParam("hoaDonId"); // Lấy ID từ URL
    let selectedHoaDonId = hoaDonIdFromUrl ? hoaDonIdFromUrl : localStorage.getItem("selectedHoaDonId");

    // Kiểm tra nếu trang KHÔNG có `hoaDonId` trong URL -> Xóa localStorage (không giữ sáng)
    if (!hoaDonIdFromUrl) {
        localStorage.removeItem("selectedHoaDonId");
        return; // Không tiếp tục làm sáng hóa đơn
    }

    // Nếu có ID được chọn, làm sáng hóa đơn tương ứng
    if (selectedHoaDonId) {
        document.querySelectorAll(".card.border-primary").forEach(card => {
            const hoaDonId = card.querySelector('input[name="hoaDonId"]').value;
            if (hoaDonId === selectedHoaDonId) {
                card.classList.add("selected-hoa-don");
            }
        });
    }
});

$(document).ready(function () {
    var table1 = $('#productTable').DataTable({
        "paging": true,
        "lengthMenu": [5, 10, 20],
        "pageLength": 5,
        "autoWidth": false,
        "responsive": true,
        "language": {
            "sProcessing": "Đang xử lý...",
            "sLengthMenu": "Hiển thị _MENU_ dòng",
            "sZeroRecords": "Không tìm thấy dữ liệu",
            "sInfo": "Hiển thị _START_ đến _END_ trong tổng _TOTAL_ dòng",
            "sInfoEmpty": "Không có dữ liệu để hiển thị",
            "sInfoFiltered": "(lọc từ _MAX_ dòng)",
            "sSearch": "Tìm kiếm:",
            "oPaginate": {
                "sFirst": "Đầu",
                "sPrevious": "Trước",
                "sNext": "Tiếp",
                "sLast": "Cuối"
            }
        }
    });

    var table2 = $('#productTable1').DataTable({
        "paging": true,
        "lengthMenu": [5, 10, 20],
        "pageLength": 5,
        "autoWidth": false,
        "responsive": true,
        "language": {
            "sProcessing": "Đang xử lý...",
            "sLengthMenu": "Hiển thị _MENU_ dòng",
            "sZeroRecords": "Không tìm thấy dữ liệu",
            "sInfo": "Hiển thị _START_ đến _END_ trong tổng _TOTAL_ dòng",
            "sInfoEmpty": "Không có dữ liệu để hiển thị",
            "sInfoFiltered": "(lọc từ _MAX_ dòng)",
            "sSearch": "Tìm kiếm:",
            "oPaginate": {
                "sFirst": "Đầu",
                "sPrevious": "Trước",
                "sNext": "Tiếp",
                "sLast": "Cuối"
            }
        }
    });

    // Lọc theo kích thước
    $('#filterKichThuoc').on('change', function () {
        var value = $(this).val();
        table1.column(2).search(value).draw();
    });

    // Lọc theo màu sắc
    $('#filterMauSac').on('change', function () {
        var value = $(this).val();
        table1.column(2).search(value).draw(); // Cột màu sắc có thể ở vị trí khác, kiểm tra lại index
    });


    // Giữ modal mở khi lọc
    $('#addProductModal').on('hidden.bs.modal', function () {
        setTimeout(() => {
            if ($('.modal-backdrop').length) {
                $('body').addClass('modal-open');
            }
        }, 100);
    });

});
