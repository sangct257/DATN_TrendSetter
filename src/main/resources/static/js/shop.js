//Tạo hóa đơn
function createHoaDon() {
    fetch('/admin/create', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'}
    })
        .then(response => {
            if (!response.ok) throw new Error(`Lỗi HTTP: ${response.status}`);
            return response.json();
        })
        .then(data => {
            if (data && data.id) {
                Swal.fire({
                    title: "Thành công!",
                    text: "Hóa đơn đã được tạo thành công.",
                    icon: "success",
                    timer: 1500,
                    showConfirmButton: false
                });

                setTimeout(() => addHoaDonToUI(data), 1500);
            }
        })
        .catch(error => {
            Swal.fire({
                title: "Lỗi!",
                text: "Không thể tạo hóa đơn.",
                icon: "error"
            });
        });
}

// Thêm hóa đơn mới vào danh sách hiển thị
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
                            <strong>${hoaDon.tongSanPham}</strong>
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
    fetch(`/admin/delete/${hoaDonId}`, {
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
    let paymentOptions = $("input[name='phuongThucThanhToanId']");
    let paymentDetails = $("#paymentDetails");
    let paymentMethodText = $("#paymentMethodText");

    // Lấy tổng tiền hóa đơn từ Thymeleaf
    let tongTienHoaDon = parseFloat('[[${hoaDon != null ? hoaDon.tongTien : 0}]]');

    // Xử lý khi chọn phương thức thanh toán
    paymentOptions.on("change", function () {
        let selectedText = $("label[for='" + this.id + "']").text().trim();
        paymentDetails.empty();

        if (selectedText === "Tiền Mặt") {
            paymentDetails.html(`
                <div class="mb-3">
                    <label for="cashAmount" class="form-label"><i class="fas fa-money-bill-wave"></i> Nhập số tiền khách đưa:</label>
                    <input type="number" id="cashAmount" name="cashAmount" class="form-control" placeholder="Nhập số tiền khách đưa" min="0">
                </div>
                <div class="mb-3">
                    <label class="form-label"><i class="fas fa-coins"></i> Số tiền thối lại:</label>
                    <input type="text" id="changeAmount" class="form-control" readonly>
                </div>
            `);

            let cashInput = $("#cashAmount");
            let changeOutput = $("#changeAmount");

            cashInput.on("input", function () {
                let cashGiven = parseFloat($(this).val()) || 0;
                let phiShip = parseFloat($("#phiShip").val()) || 0;
                let maGiamGia = parseFloat($("#listPhieuGiamGia").val()) || 0;
                let totalAmount = tongTienHoaDon + phiShip - maGiamGia;

                let change = cashGiven - totalAmount;
                changeOutput.val(change < 0 ? `Thiếu ${Math.abs(change).toLocaleString()} VND` : `${change.toLocaleString()} VND`)
                    .css("color", change < 0 ? "red" : "green");
            });

        } else if (selectedText === "Chuyển Khoản") {
            paymentDetails.html(`
                <div class="mb-3 text-center">
                    <img id="qrCodeImage" src="" alt="QR Code" class="img-fluid" style="max-width: 250px;">
                </div>
            `);
        }
    });

    // Xử lý khi nhấn nút "Cập nhật" để chọn phương thức thanh toán
    $("#addPaymentMethodButton").on("click", function () {
        let formData = $("#paymentMethodForm").serialize();
        let selectedPayment = $("input[name='phuongThucThanhToanId']:checked").val();

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
                        }).then(() => {
                            location.reload();
                        });
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
    const tenPhieuGiamGia = document.getElementById("tenPhieuGiamGia").value;

    if (!hoaDonId || isNaN(hoaDonId)) {
        Swal.fire("Lỗi!", "ID hóa đơn không hợp lệ!", "error");
        return;
    }

    if (!tenPhieuGiamGia) {
        Swal.fire("Cảnh báo!", "Vui lòng chọn phiếu giảm giá.", "warning");
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
                text: data.success ? "Phiếu giảm giá đã được áp dụng!" : (data.error || "Lỗi khi áp dụng phiếu giảm giá!"),
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
        const soNha = formData.get("soNha").trim();
        const tenDuong = formData.get("tenDuong").trim();
        const thanhPho = formData.get("thanhPho").trim();
        const huyen = formData.get("huyen").trim();
        const phuong = formData.get("phuong").trim();

        // Xóa thông báo lỗi trước khi kiểm tra
        document.getElementById("shippingNguoiNhanError").textContent = "";
        document.getElementById("shippingSoDienThoaiError").textContent = "";
        document.getElementById("soNhaError").textContent = "";
        document.getElementById("tenDuongError").textContent = "";
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
        // Kiểm tra số nhà (chỉ được chứa số)
        const soNhaRegex = /^[0-9]+$/;
        if (!soNha) {
            document.getElementById("soNhaError").textContent = "Số nhà không được để trống!";
            isValid = false;
        } else if (!soNhaRegex.test(soNha)) {
            document.getElementById("soNhaError").textContent = "Số nhà chỉ được chứa số!";
            isValid = false;
        }

        if (!tenDuong) {
            document.getElementById("tenDuongError").textContent = "Tên đường không được để trống!";
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

    function fetchProvinces(selectedValue = "") {
        fetch("/api/ghn/provinces")
            .then(response => response.json())
            .then(data => {
                thanhPhoSelect.innerHTML = '<option value="">Chọn Tỉnh/Thành phố</option>';
                let selectedProvinceId = "";

                data.data.forEach(province => {
                    const selected = province.ProvinceName === selectedValue ? "selected" : "";
                    if (selected) selectedProvinceId = province.ProvinceID;
                    thanhPhoSelect.innerHTML += `<option value="${province.ProvinceName}" data-id="${province.ProvinceID}" ${selected}>${province.ProvinceName}</option>`;
                });

                if (selectedProvinceId) fetchDistricts(selectedProvinceId, huyenHidden.value);
            })
            .catch(error => console.error("Lỗi khi lấy danh sách tỉnh/thành phố:", error));
    }

    function fetchDistricts(provinceId, selectedValue = "") {
        fetch(`/api/ghn/districts?province_id=${provinceId}`)
            .then(response => response.json())
            .then(data => {
                huyenSelect.innerHTML = '<option value="">Chọn Quận/Huyện</option>';
                let selectedDistrictId = "";

                data.data.forEach(district => {
                    const selected = district.DistrictName === selectedValue ? "selected" : "";
                    if (selected) selectedDistrictId = district.DistrictID;
                    huyenSelect.innerHTML += `<option value="${district.DistrictName}" data-id="${district.DistrictID}" ${selected}>${district.DistrictName}</option>`;
                });

                if (selectedDistrictId) fetchWards(selectedDistrictId, phuongHidden.value);
            })
            .catch(error => console.error("Lỗi khi lấy danh sách quận/huyện:", error));
    }

    function fetchWards(districtId, selectedValue = "") {
        fetch(`/api/ghn/wards?district_id=${districtId}`)
            .then(response => response.json())
            .then(data => {
                phuongSelect.innerHTML = '<option value="">Chọn Phường/Xã</option>';

                data.data.forEach(ward => {
                    const selected = ward.WardName === selectedValue ? "selected" : "";
                    phuongSelect.innerHTML += `<option value="${ward.WardName}" data-id="${ward.WardCode}" ${selected}>${ward.WardName}</option>`;
                });
            })
            .catch(error => console.error("Lỗi khi lấy danh sách phường/xã:", error));
    }

    // Cập nhật giá trị ẩn khi thay đổi lựa chọn
    thanhPhoSelect.addEventListener("change", function () {
        const selectedOption = this.options[this.selectedIndex];
        const provinceId = selectedOption.getAttribute("data-id");

        thanhPhoHidden.value = selectedOption.value;
        huyenHidden.value = "";
        phuongHidden.value = "";

        huyenSelect.innerHTML = '<option value="">Chọn Quận/Huyện</option>';
        phuongSelect.innerHTML = '<option value="">Chọn Phường/Xã</option>';

        if (provinceId) {
            fetchDistricts(provinceId);
        }
    });

    huyenSelect.addEventListener("change", function () {
        const districtId = this.options[this.selectedIndex].getAttribute("data-id");
        huyenHidden.value = this.value;
        phuongHidden.value = "";
        phuongSelect.innerHTML = '<option value="">Chọn Phường/Xã</option>';

        if (districtId) {
            fetchWards(districtId);
            calculateShippingFee(districtId, phuongHidden.value);
        }
    });

    phuongSelect.addEventListener("change", function () {
        const wardCode = this.options[this.selectedIndex].getAttribute("data-id");
        phuongHidden.value = this.value;
        calculateShippingFee(huyenSelect.options[huyenSelect.selectedIndex].getAttribute("data-id"), wardCode);
    });

    // Hàm tính phí ship
    // function calculateShippingFee(districtId, wardCode) {
    //     if (!districtId || !wardCode) {
    //         document.getElementById("shippingFee").textContent = "Vui lòng chọn đầy đủ địa chỉ";
    //         return;
    //     }
    //
    //     fetch("https://online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/fee", {
    //         method: "POST",
    //         headers: {
    //             "Content-Type": "application/json",
    //             "Token": "10b65b43-eaf7-11ef-8752-da588d8b708e",
    //             "ShopId": "5635779"
    //         },
    //         body: JSON.stringify({
    //             service_id: 53321, // Mặc định dịch vụ tiêu chuẩn
    //             service_type_id: 2, // Nếu service_id không hợp lệ, GHN có thể dùng service_type_id thay thế
    //             insurance_value: 10000,
    //             coupon: null,
    //             from_district_id: 1442,
    //             to_district_id: parseInt(districtId),
    //             to_ward_code: wardCode,
    //             length: 20,
    //             height: 50,
    //             weight: 500,
    //             width: 20,
    //         })
    //     })
    //         .then(response => response.json())
    //         .then(data => {
    //             if (data.code !== 200 || !data.data?.total) {
    //                 document.getElementById("shippingFee").textContent = "Không tính được phí ship";
    //                 return;
    //             }
    //
    //             const fee = data.data.total;
    //             document.getElementById("shippingFee").textContent = fee.toLocaleString("vi-VN") + " VND";
    //         })
    //         .catch(error => {
    //             console.error("Lỗi khi tính phí ship:", error);
    //             document.getElementById("shippingFee").textContent = "Không thể tính phí ship";
    //         });
    // }


    // Nếu có dữ liệu cũ, hiển thị trước rồi cho phép thay đổi
    if (thanhPhoHidden.value) {
        fetchProvinces(thanhPhoHidden.value);
    } else {
        fetchProvinces();
    }
});

<!-- API Update New Khách Hàng -->
document.addEventListener("DOMContentLoaded", function () {
    const form = document.querySelector("#addCustomerForm");

    if (!form) {
        console.warn("Không tìm thấy form #addCustomerForm");
        return;
    }

    form.addEventListener("submit", function (event) {
        event.preventDefault(); // Ngăn chặn gửi form mặc định

        const formData = new FormData(form);
        const hoaDonId = formData.get("hoaDonId");
        const nguoiNhan = formData.get("nguoiNhan").trim();
        const soDienThoai = formData.get("soDienThoai").trim();

        // Xóa lỗi cũ
        document.getElementById("nguoiNhanError").textContent = "";
        document.getElementById("soDienThoaiError").textContent = "";

        let isValid = true;

        // Kiểm tra ID hóa đơn hợp lệ
        if (!hoaDonId || isNaN(hoaDonId)) {
            Swal.fire("Lỗi!", "ID hóa đơn không hợp lệ!", "error");
            return;
        }

        // Kiểm tra họ tên (chỉ chứa chữ cái, khoảng trắng, có ít nhất 2 từ)
        const nameRegex = /^[A-Za-zÀ-ỹ\s]+$/;
        const words = nguoiNhan.split(/\s+/).filter(word => word.length > 0); // Tách thành các từ

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

        // Kiểm tra số điện thoại (bắt đầu bằng 0, có tổng 10-11 số)
        const phoneRegex = /^(0\d{9,10})$/;
        if (!soDienThoai) {
            document.getElementById("soDienThoaiError").textContent = "Số điện thoại không được để trống!";
            isValid = false;
        } else if (!phoneRegex.test(soDienThoai)) {
            document.getElementById("soDienThoaiError").textContent = "Số điện thoại không hợp lệ!";
            isValid = false;
        }

        if (!isValid) return; // Dừng nếu có lỗi

        // Gửi yêu cầu đến API
        fetch("/add-new-customer", {
            method: "POST",
            body: formData,
        })
            .then(response => response.json().then(data => {
                if (!response.ok) {
                    throw new Error(data.error || "Lỗi từ server");
                }
                return data;
            }))
            .then(data => {
                Swal.fire({
                    title: data.success ? "Thành công!" : "Lỗi!",
                    text: data.success || data.error || "Có lỗi xảy ra!",
                    icon: data.success ? "success" : "error"
                }).then(() => {
                    if (data.success) {
                        setTimeout(() => location.reload(), 500);
                    }
                });
            })
            .catch(error => {
                console.error("Lỗi:", error);
                Swal.fire("Lỗi!", "Lỗi khi cập nhật thông tin khách hàng!", "error");
            });
    });
});

// Lưu hoaDonId vào sessionStorage khi chọn hóa đơn
function saveHoaDonId(hoaDonId) {
    sessionStorage.setItem('selectedHoaDonId', hoaDonId);
    console.log("HoaDonId đã lưu:", hoaDonId);
}

// API Thêm Sản Phẩm
function addProductOrder(button) {
    const row = button.closest('tr');
    if (!row) {
        console.error("Không tìm thấy hàng sản phẩm.");
        return;
    }

    const sanPhamChiTietId = parseInt(row.querySelector('td:first-child')?.textContent.trim(), 10);
    const soLuong = parseInt(row.querySelector('input[name="soLuong"]')?.value, 10);
    const hoaDonId = parseInt(sessionStorage.getItem('selectedHoaDonId'), 10);

    if (!hoaDonId || isNaN(hoaDonId)) {
        Swal.fire({
            title: "Lỗi!",
            text: "Không tìm thấy hóa đơn hợp lệ!",
            icon: "error"
        });
        return;
    }

    console.log("Gửi yêu cầu thêm sản phẩm:", {sanPhamChiTietId, hoaDonId, soLuong});

    fetch('/add-product-order', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({sanPhamChiTietId, hoaDonId, soLuong})
    })
        .then(response => response.json())
        .then(response => {
            console.log("Phản hồi API:", response);
            handleResponse(response, hoaDonId);
        })
        .catch(handleError);
}

// API Cập Nhật Số Lượng
function updateQuantityOrder(input) {
    const row = input.closest('tr');
    const hoaDonChiTietId = parseInt(row.querySelector('input[name="hoaDonChiTietId"]').value, 10);
    const soLuong = parseInt(input.value, 10);
    const hoaDonId = parseInt(sessionStorage.getItem('selectedHoaDonId'), 10);

    if (isNaN(hoaDonChiTietId) || isNaN(hoaDonId) || isNaN(soLuong) || soLuong < 1) {
        Swal.fire({
            title: "Lỗi!",
            text: "Thông tin không hợp lệ!",
            icon: "error"
        });
        return;
    }

    const data = {hoaDonChiTietId, hoaDonId, soLuong};

    Swal.fire({
        title: "Đang cập nhật...",
        text: "Vui lòng đợi trong giây lát.",
        icon: "info",
        showConfirmButton: false,
        allowOutsideClick: false
    });

    fetch('/update-product-order', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(data)
    })
        .then(response => response.json())
        .then(response => handleResponse(response, hoaDonId))
        .catch(handleError);
}

// API Xóa Sản Phẩm
function deleteProductOrder(button) {
    const row = button.closest('tr');
    const hoaDonChiTietId = parseInt(row.querySelector('input[name="hoaDonChiTietId"]').value, 10);
    const hoaDonId = parseInt(sessionStorage.getItem('selectedHoaDonId'), 10);

    if (isNaN(hoaDonChiTietId) || isNaN(hoaDonId)) {
        Swal.fire({
            title: "Lỗi!",
            text: "Thông tin không hợp lệ!",
            icon: "error"
        });
        return;
    }

    Swal.fire({
        title: "Xác nhận xóa?",
        text: "Bạn có chắc chắn muốn xóa sản phẩm này khỏi đơn hàng không?",
        icon: "warning",
        showCancelButton: true,
        confirmButtonText: "Xóa",
        cancelButtonText: "Hủy",
        reverseButtons: true
    }).then((result) => {
        if (result.isConfirmed) {
            const data = {hoaDonChiTietId, hoaDonId};

            Swal.fire({
                title: "Đang xóa...",
                text: "Vui lòng đợi trong giây lát.",
                icon: "info",
                showConfirmButton: false,
                allowOutsideClick: false
            });

            fetch('/delete-product-order', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(data)
            })
                .then(response => response.json())
                .then(response => handleResponse(response, hoaDonId))
                .catch(handleError);
        }
    });
}

// Xử lý phản hồi chung cho các API
function handleResponse(response, hoaDonId) {
    if (response.successMessage) {
        Swal.fire({
            title: "Thành công!",
            text: response.successMessage,
            icon: "success",
            timer: 1500,
            showConfirmButton: false
        }).then(() => {
            // Lưu vị trí cuộn trước khi chuyển hướng
            sessionStorage.setItem('scrollPosition', window.scrollY);
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
    Swal.fire({
        title: "Lỗi!",
        text: "Có lỗi xảy ra: " + error.message,
        icon: "error"
    });
}

// Chuyển hướng đến trang phù hợp dựa trên trạng thái
function redirectToAppropriatePage(trangThai, hoaDonId) {
    let redirectUrl;
    if (trangThai === "Đang Xử Lý") {
        redirectUrl = `/admin/sell-counter?hoaDonId=${hoaDonId}`;
    } else if (trangThai === "Chờ Xác Nhận") {
        redirectUrl = `/admin/order-details?hoaDonId=${hoaDonId}`;
    } else {
        redirectUrl = `/admin/sell-counter?hoaDonId=${hoaDonId}`;
    }

    // Sau khi chuyển hướng, trang sẽ giữ lại vị trí cuộn
    window.location.href = redirectUrl;
}

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
    $('#productTable').DataTable({
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
});
            