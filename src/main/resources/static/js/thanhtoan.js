document.addEventListener("DOMContentLoaded", function () {
    validateCartBeforeCheckout();
    loadCheckoutCart();
});

function validateCartBeforeCheckout() {
    let currentCart = JSON.parse(localStorage.getItem("cart")) || [];

    if (currentCart.length === 0) {
        console.log("🛒 Giỏ hàng trống! Xoá dữ liệu thanh toán...");
        sessionStorage.removeItem("checkoutCart"); // Xoá dữ liệu cũ
    }
}

function loadCheckoutCart() {
    let checkoutData = JSON.parse(sessionStorage.getItem("checkoutCart"));

    if (!checkoutData || !checkoutData.cart || checkoutData.cart.length === 0) {
        document.getElementById("order-items").innerHTML = "<p>Không có sản phẩm nào trong giỏ hàng.</p>";
        document.getElementById("subtotal").innerText = "0 VND";
        document.getElementById("shipping-fee").innerText = "0 VND";
        document.getElementById("total").innerText = "0 VND";
        document.getElementById("discount-container").style.display = "none";
        return;
    }

    let cart = checkoutData.cart;
    let orderItemsContainer = document.getElementById("order-items");
    let subtotal = 0;

    orderItemsContainer.innerHTML = "";

    cart.forEach((item) => {
        let price = Number(item.price.toString().replace(/\D/g, ""));
        let total = price * item.quantity;
        subtotal += total;

        orderItemsContainer.innerHTML += `
            <div class="d-flex justify-content-between">
                <p>${item.name} (${item.size}, ${item.color}) x ${item.quantity}</p>
                <p>${total.toLocaleString('vi-VN')} VND</p>
            </div>
        `;
    });

    let shippingFee = Number(checkoutData.shippingFee.replace(/\D/g, ""));
    let discount = checkoutData.discount.value || 0;
    let totalAmount = subtotal + shippingFee - discount;

    document.getElementById("subtotal").innerText = `${subtotal.toLocaleString('vi-VN')} VND`;
    document.getElementById("shipping-fee").innerText = `${shippingFee.toLocaleString('vi-VN')} VND`;
    document.getElementById("total").innerHTML = `<span style="color: red; font-weight: bold;">${totalAmount.toLocaleString('vi-VN')} VND</span>`;

    if (discount > 0) {
        document.getElementById("discount-container").style.display = "flex";
        document.getElementById("discount-display").innerText = `-${discount.toLocaleString('vi-VN')} VND`;
    } else {
        document.getElementById("discount-container").style.display = "none";
    }
}



document.addEventListener("DOMContentLoaded", function () {
    loadProvinces(); // Tải danh sách tỉnh/thành khi trang tải

    async function loadProvinces() {
        let response = await fetch("https://provinces.open-api.vn/api/?depth=1");
        let data = await response.json();
        let provinceSelect = document.getElementById("province");

        data.forEach(province => {
            let option = document.createElement("option");
            option.value = province.code;
            option.textContent = province.name;
            provinceSelect.appendChild(option);
        });
    }
});

async function loadDistricts() {
    let provinceCode = document.getElementById("province").value;
    let districtSelect = document.getElementById("district");
    districtSelect.innerHTML = '<option value="">Chọn Quận/Huyện</option>';

    if (!provinceCode) return;

    let response = await fetch(`https://provinces.open-api.vn/api/p/${provinceCode}?depth=2`);
    let data = await response.json();

    data.districts.forEach(district => {
        let option = document.createElement("option");
        option.value = district.code;
        option.textContent = district.name;
        districtSelect.appendChild(option);
    });
}

async function loadWards() {
    let districtCode = document.getElementById("district").value;
    let wardSelect = document.getElementById("ward");
    wardSelect.innerHTML = '<option value="">Chọn Phường/Xã</option>';

    if (!districtCode) return;

    let response = await fetch(`https://provinces.open-api.vn/api/d/${districtCode}?depth=2`);
    let data = await response.json();

    data.wards.forEach(ward => {
        let option = document.createElement("option");
        option.value = ward.code;
        option.textContent = ward.name;
        wardSelect.appendChild(option);
    });
}
function validateForm() {
    clearErrors();
    let isValid = true;

    let requiredFields = [
        { id: "fullName", message: "Vui lòng nhập họ tên." },
        { id: "phoneNumber", message: "Vui lòng nhập số điện thoại." },
        { id: "specific-address", message: "Vui lòng nhập địa chỉ cụ thể." },
        { id: "province", message: "Vui lòng chọn tỉnh/thành phố." },
        { id: "district", message: "Vui lòng chọn quận/huyện." },
        { id: "ward", message: "Vui lòng chọn phường/xã." }
    ];

    requiredFields.forEach(field => {
        let element = document.getElementById(field.id);
        if (!element || element.value.trim() === "") {
            showError(field.id, field.message);
            isValid = false;
        }
    });

    // Kiểm tra email (có thể để trống nhưng nếu nhập phải hợp lệ)
    let emailField = document.getElementById("email");
    if (emailField.value.trim() !== "" && !isValidEmail(emailField.value)) {
        showError("email", "Vui lòng nhập email hợp lệ.");
        isValid = false;
    }

    return isValid;
}

// ⚠️ Hiển thị lỗi bên dưới input
function showError(inputId, message) {
    const inputElement = document.getElementById(inputId);
    if (!inputElement) return;
    inputElement.classList.add("is-invalid");

    let oldError = inputElement.nextElementSibling;
    if (oldError && oldError.classList.contains("invalid-feedback")) {
        oldError.remove();
    }

    inputElement.insertAdjacentHTML("afterend", `<div class="invalid-feedback">${message}</div>`);
}

// ✅ Xóa lỗi khi người dùng nhập lại
function clearErrors() {
    document.querySelectorAll(".is-invalid").forEach(element => {
        element.classList.remove("is-invalid");
    });
    document.querySelectorAll(".invalid-feedback").forEach(element => {
        element.remove();
    });
}

// 📧 Hàm kiểm tra email hợp lệ
function isValidEmail(email) {
    let regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return regex.test(email);
}

// 📌 Xử lý đặt hàng
async function placeOrder() {
    if (!validateForm()) return;

    let cartData = localStorage.getItem("cart");
    if (!cartData || JSON.parse(cartData).length === 0) {
        Swal.fire({ icon: "error", title: "Giỏ hàng trống!", text: "Vui lòng thêm sản phẩm vào giỏ trước khi đặt hàng." });
        return;
    }

    let cart = JSON.parse(cartData);
    let subtotal = cart.reduce((sum, item) => sum + (Number(item.price.replace(/\D/g, "")) * item.quantity), 0);
    let shippingFee = 30000;
    let discount = 0;
    let discountId = null;

    let discountData = JSON.parse(sessionStorage.getItem("checkoutCart"));
    if (discountData?.discount) {
        discount = Number(discountData.discount.value) || 0;
        discountId = discountData.discount.id || null;
    }

    let totalAmount = subtotal + shippingFee - discount;

    let orderInfo = {
        nguoiNhan: document.getElementById("fullName").value.trim(),
        soDienThoai: document.getElementById("phoneNumber").value.trim(),
        email: document.getElementById("email").value.trim(),
        diaChiCuThe: document.getElementById("specific-address").value.trim(),
        thanhPho: document.getElementById("province").options[document.getElementById("province").selectedIndex].text,
        huyen: document.getElementById("district").options[document.getElementById("district").selectedIndex].text,
        phuong: document.getElementById("ward").options[document.getElementById("ward").selectedIndex].text,
        tongTien: totalAmount,
        phiShip: shippingFee,
        idPhuongThucThanhToan: document.getElementById("cod").checked ? 1 : 2,
        idPhieuGiamGia: discountId,
        hoaDonChiTiet: cart.map(item => ({
            idSanPhamChiTiet: item.idSanPhamChiTiet,
            soLuong: item.quantity,
            gia: Number(item.price.replace(/\D/g, ""))
        }))
    };

    // 🔹 Nếu thanh toán COD → tạo hóa đơn ngay
    if (orderInfo.idPhuongThucThanhToan === 1) {
        try {
            // 🔥 Chuẩn bị danh sách sản phẩm cần giảm số lượng
            let stockUpdates = orderInfo.hoaDonChiTiet.map(item => ({
                idSanPhamChiTiet: item.idSanPhamChiTiet,
                soLuong: item.soLuong
            }));

            // 🔥 Gửi 1 request duy nhất để giảm số lượng nhiều sản phẩm cùng lúc
            let stockResponse = await fetch("/api/san-pham-chi-tiet/reduce-stock", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(stockUpdates)
            });

            let stockResult = await stockResponse.json();

            if (!stockResponse.ok) {
                Swal.fire({ icon: "error", title: "Lỗi giảm số lượng!", text: "Không thể giảm số lượng sản phẩm." });
                return;
            }

            // ✅ Kiểm tra phản hồi từ server
            let failedProducts = Object.entries(stockResult)
                .filter(([_, message]) => message.includes("Không đủ hàng"))
                .map(([productId]) => productId);

            if (failedProducts.length > 0) {
                Swal.fire({
                    icon: "error",
                    title: "Không đủ hàng!",
                    text: `Sản phẩm không đủ hàng: ${failedProducts.join(", ")}`
                });
                return;
            }

            // ✅ Nếu giảm số lượng thành công, tiếp tục tạo hóa đơn
            let response = await fetch("/api/hoa-don/create", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(orderInfo)
            });

            let result = await response.json();

            if (response.ok) {
                localStorage.removeItem("cart");
                sessionStorage.removeItem("checkoutCart");
                sessionStorage.removeItem("discountCode");
                localStorage.removeItem("discountCode");
                Swal.fire({
                    icon: "success",
                    title: "Đặt hàng thành công!",
                    text: `Mã hóa đơn của bạn: ${result.maHoaDon}`,
                    showCancelButton: true,
                    confirmButtonText: "Xem đơn hàng",
                    cancelButtonText: "Về trang chủ"
                }).then((res) => {
                    window.location.href = res.isConfirmed
                        ? `/don-hang?maHoaDon=${result.maHoaDon}` // Chuyển hướng tới trang "don-hang" với tham số maHoaDon
                        : "/trang-chu"; // Nếu không confirmed, chuyển hướng về trang chủ
                });
            } else {
                Swal.fire({ icon: "error", title: "Lỗi!", text: result.message || "Đã có lỗi xảy ra." });
            }
        } catch (error) {
            Swal.fire({ icon: "error", title: "Lỗi hệ thống!", text: "Không thể kết nối đến máy chủ." });
        }
    }
    // 🔹 Nếu thanh toán VNPay → chuyển hướng tới VNPay
    else {
        sessionStorage.setItem("pendingOrder", JSON.stringify(orderInfo));

        let vnpayResponse = await fetch("/api/payment/create-payment", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ orderId: Date.now(), amount: totalAmount })
        });

        let vnpayData = await vnpayResponse.json();

        if (vnpayResponse.ok) {
            window.location.href = vnpayData.paymentUrl; // 🔥 Chuyển hướng tới VNPay
        } else {
            Swal.fire({ icon: "error", title: "Lỗi VNPay!", text: vnpayData.message || "Không thể tạo thanh toán VNPay." });
        }
    }
}