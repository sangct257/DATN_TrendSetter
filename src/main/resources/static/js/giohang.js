document.addEventListener("DOMContentLoaded", function () {
    renderCart();
    console.log("🛒 Giỏ hàng khi tải trang:", JSON.parse(localStorage.getItem("cart")));

    document.getElementById("cart-body").addEventListener("click", function (event) {
        let row = event.target.closest("tr");
        if (!row) return;

        let idSanPhamChiTiet = row.dataset.idsanphamchitiet;
        let size = row.dataset.size;
        let color = row.dataset.color;

        let cart = JSON.parse(localStorage.getItem("cart")) || [];
        let product = cart.find(item =>
            item.idSanPhamChiTiet == idSanPhamChiTiet &&
            item.size == size &&
            item.color == color
        );

        if (!product) return;

        if (event.target.classList.contains("btn-plus")) {
            if (product.quantity < 20 && product.quantity < product.availableQuantity) {
                product.quantity += 1;
            } else {
                Swal.fire({
                    icon: "warning",
                    title: "Giới hạn số lượng!",
                    text: "Bạn chỉ có thể mua tối đa 20 sản phẩm mỗi loại, hoặc không đủ hàng.",
                    confirmButtonText: "OK"
                });
            }
        } else if (event.target.classList.contains("btn-minus")) {
            if (product.quantity > 1) {
                product.quantity -= 1;
            }
        } else if (event.target.classList.contains("btn-remove")) {
            cart = cart.filter(item => !(item.idSanPhamChiTiet == idSanPhamChiTiet && item.size == size && item.color == color));
            localStorage.setItem("cart", JSON.stringify(cart));
            renderCart();  // Cập nhật lại giỏ hàng sau khi xóa sản phẩm
            fetchCoupons(); // Cập nhật lại phiếu giảm giá sau khi thay đổi giỏ hàng
            updateCartBadge();
        }

        localStorage.setItem("cart", JSON.stringify(cart));
        renderCart();
        fetchCoupons(); // Cập nhật lại phiếu giảm giá sau khi thay đổi giỏ hàng
        resetDiscount(); // reset giá giảm
        updateCartBadge(); // Cập nhật lại số lượng giỏ hàng ở đây nếu cần
    });

    document.getElementById("checkout-button").addEventListener("click", function (event) {
        event.preventDefault();

        let cart = localStorage.getItem("cart");
        if (!cart) return;

        let discountCode = localStorage.getItem("discountCode") || null;
        let discountId = localStorage.getItem("discountId") || null;
        let discountValue = localStorage.getItem("discountValue") || "0";
        let shippingFee = document.getElementById("shipping-fee").textContent.trim();
        let totalAmount = document.getElementById("total").textContent.trim();

        sessionStorage.setItem("checkoutCart", JSON.stringify({
            cart: JSON.parse(cart),
            discount: discountCode ? { id: discountId, code: discountCode, value: parseInt(discountValue.replace(/\D/g, "")) } : { id: null, code: null, value: 0 },
            shippingFee,
            total: totalAmount
        }));

        window.location.href = "thanh-toan";
    });

});

function resetDiscount() {
    // Set giá giảm về 0
    document.getElementById("discount-price").innerText = "0đ";
    document.getElementById("total-price-after-discount").innerText = document.getElementById("total-price").innerText;

    // Xóa mã giảm giá đã chọn trong localStorage (nếu bạn lưu)
    localStorage.removeItem("selectedCoupon");
}

function renderCart() {
    let cartData = localStorage.getItem("cart");
    let cartBody = document.querySelector("#cart-body");
    let subtotal = 0;
    cartBody.innerHTML = "";

    if (!cartData || JSON.parse(cartData).length === 0) {
        cartBody.innerHTML = `<tr><td colspan="7" class="text-center">Giỏ hàng của bạn đang trống.</td></tr>`;
        document.getElementById("subtotal").innerText = "0 VND";
        document.getElementById("total").innerText = "0 VND";
        document.getElementById("shipping-fee").innerText = "0 VND";

        // Xóa phiếu giảm giá nếu giỏ hàng trống
        localStorage.removeItem("discountCode");
        localStorage.removeItem("discountValue");
        localStorage.removeItem("discountId");

        // Cập nhật lại tổng tiền sau khi xóa phiếu giảm giá
        updateTotal(0);  // Đảm bảo tổng tiền được cập nhật với giá trị giảm giá bằng 0
        updateCartBadge(); // Cập nhật lại số lượng giỏ hàng khi giỏ hàng trống
        return;
    }

    let cart = JSON.parse(cartData);
    cart.forEach((item, index) => {
        let price = Number(item.price.toString().replace(/\D/g, ""));
        let total = price * item.quantity;
        subtotal += total;

        cartBody.innerHTML += `
<tr data-idsanphamchitiet="${item.idSanPhamChiTiet}" data-size="${item.size}" data-color="${item.color}">
    <td class="align-middle">${index + 1}</td>
    <td class="align-middle">
        <img src="${item.image || 'default.jpg'}" alt="Ảnh sản phẩm" style="width: 50px; height: 50px; object-fit: cover;">
    </td>
    <td class="align-middle">${item.name || "Sản phẩm không xác định"}</td>
    <td class="align-middle">
        Màu: ${item.color || "Không rõ"}, Size: ${item.size || "Không rõ"}

    </td>
    <td class="align-middle">
        <div class="input-group quantity mx-auto" style="width: 100px;">
            <div class="input-group-btn">
                <button class="btn btn-sm btn-primary btn-minus"><i class="fa fa-minus"></i></button>
            </div>
            <input type="text" class="form-control form-control-sm bg-secondary text-center quantity-input" value="${item.quantity}" readonly>
            <div class="input-group-btn">
                <button class="btn btn-sm btn-primary btn-plus"><i class="fa fa-plus"></i></button>
            </div>
        </div>
    </td>
    <td class="align-middle">${price.toLocaleString('vi-VN')} VND</td>
    <td class="align-middle">${total.toLocaleString('vi-VN')} VND</td>
    <td class="align-middle">
        <button class="btn btn-sm btn-danger btn-remove"><i class="fa fa-times"></i></button>
    </td>
</tr>
`;
    });

    let shippingFee = cart.length > 0 ? 30000 : 0;
    document.getElementById("shipping-fee").innerText = shippingFee.toLocaleString('vi-VN') + " VND";
    document.getElementById("subtotal").innerText = subtotal.toLocaleString('vi-VN') + " VND";
    document.getElementById("total").innerText = (subtotal + shippingFee).toLocaleString('vi-VN') + " VND";

    // Cập nhật lại tổng tiền nếu có giảm giá
    let savedDiscountValue = localStorage.getItem("discountValue") ? Number(localStorage.getItem("discountValue")) : 0;
    updateTotal(savedDiscountValue);

    // Cập nhật lại số lượng giỏ hàng trên header
    updateCartBadge();
}

function updateTotal(discountValue = 0) {
    let subtotal = getCartTotal();
    let shippingFee = Number(document.getElementById("shipping-fee").innerText.replace(/\D/g, ""));
    let finalTotal = subtotal + shippingFee - discountValue;

    document.getElementById("total").innerText = `${finalTotal.toLocaleString()} VND`;

    const discountContainer = document.getElementById("discount-container");
    const discountDisplay = document.getElementById("discount-display");

    if (discountValue > 0) {
        discountContainer.style.display = 'flex';
        discountDisplay.innerText = `-${discountValue.toLocaleString()} VND`;
    } else {
        discountContainer.style.display = 'none';
        discountDisplay.innerText = "-0 VND";
    }
}


function getCartTotal() {
    return Number(document.getElementById("subtotal").innerText.replace(/\D/g, ""));
}


async function fetchCoupons() {
    try {
        const response = await fetch('http://localhost:8080/api/phieu-giam-gia?trangThai=%C4%90ang%20Ho%E1%BA%A1t%20%C4%90%E1%BB%99ng');
        let coupons = await response.json();

        let cartTotal = getCartTotal(); // Lấy tổng tiền giỏ hàng

        // Lọc ra phiếu đủ điều kiện và không đủ điều kiện
        let eligibleCoupons = coupons.filter(coupon => cartTotal >= coupon.dieuKien);
        let ineligibleCoupons = coupons.filter(coupon => cartTotal < coupon.dieuKien);

        // Sắp xếp giảm dần theo giá trị giảm
        eligibleCoupons.sort((a, b) => b.giaTriGiam - a.giaTriGiam);
        ineligibleCoupons.sort((a, b) => b.giaTriGiam - a.giaTriGiam);

        // Kết hợp danh sách: ưu tiên hiển thị phiếu đủ điều kiện trước
        let sortedCoupons = [...eligibleCoupons, ...ineligibleCoupons];

        renderCoupons(sortedCoupons);
    } catch (error) {
        console.error('Lỗi khi tải phiếu giảm giá:', error);
    }
}

function renderCoupons(coupons) {
    const container = document.getElementById('coupon-container');
    container.innerHTML = '';

    let cartTotal = getCartTotal(); // Lấy tổng đơn hàng hiện tại
    console.log("🔹 Tổng đơn hàng:", cartTotal);

    let savedDiscountCode = localStorage.getItem("discountCode");
    let savedDiscountValue = localStorage.getItem("discountValue") ? Number(localStorage.getItem("discountValue")) : 0;
    let savedDiscountId = localStorage.getItem("discountId");

    // Tìm phiếu giảm giá đủ điều kiện và sắp xếp giảm dần theo giá trị giảm
    let eligibleCoupons = coupons.filter(coupon => cartTotal >= coupon.dieuKien);
    eligibleCoupons.sort((a, b) => b.giaTriGiam - a.giaTriGiam); // Sắp xếp giảm dần theo giá trị giảm

    // Nếu không có phiếu giảm giá đủ điều kiện, xóa giảm giá và cập nhật tổng
    if (eligibleCoupons.length === 0) {
        localStorage.removeItem("discountCode");
        localStorage.removeItem("discountValue");
        localStorage.removeItem("discountId");
        updateTotal(0);
    }

    // Nếu có phiếu giảm giá đủ điều kiện, tự động chọn phiếu giảm giá có giá trị giảm cao nhất
    let selectedCoupon = null;
    if (eligibleCoupons.length > 0) {
        selectedCoupon = eligibleCoupons[0]; // Chọn phiếu có giá trị giảm cao nhất
    }

    // Hiển thị phiếu giảm giá đã chọn
    coupons.forEach(coupon => {
        let isEligible = cartTotal >= coupon.dieuKien;

        const couponElement = document.createElement('div');
        couponElement.classList.add('coupon');
        couponElement.innerHTML = `
            <div class="coupon-left">${coupon.giaTriGiam.toLocaleString()}</div>
            <div class="coupon-right">
                <div class="coupon-title">${coupon.tenPhieuGiamGia} - <strong>${coupon.maPhieuGiamGia}</strong></div>
                <div class="coupon-condition">Đơn hàng từ ${coupon.dieuKien.toLocaleString()} ${coupon.donViTinh}</div>
            </div>
            <input type="radio" name="coupon" class="coupon-select" value="${coupon.maPhieuGiamGia}"
                   data-value="${coupon.giaTriGiam}" ${isEligible ? '' : 'disabled'}
                   ${coupon.maPhieuGiamGia === (selectedCoupon ? selectedCoupon.maPhieuGiamGia : savedDiscountCode) ? 'checked' : ''}>
        `;
        container.appendChild(couponElement);

        // Nếu phiếu đã lưu trữ được tìm thấy, tự động áp dụng lại
        if (coupon.maPhieuGiamGia === savedDiscountCode) {
            updateTotal(savedDiscountValue);
        }

        couponElement.addEventListener('click', function (event) {
            let radio = couponElement.querySelector(".coupon-select");

            if (radio.disabled) {
                Swal.fire({
                    icon: "error",
                    title: "Phiếu không đủ điều kiện!",
                    text: "Giỏ hàng của bạn chưa đủ điều kiện để áp dụng phiếu giảm giá này.",
                    confirmButtonText: "OK"
                });
                return;
            }

            if (event.target.classList.contains("coupon-select")) return;

            if (radio.checked) {
                radio.checked = false;
                localStorage.removeItem("discountCode");
                localStorage.removeItem("discountValue");
                localStorage.removeItem("discountId");
                updateTotal(0);
            } else {
                document.querySelectorAll(".coupon-select").forEach(r => r.checked = false);

                radio.checked = true;
                localStorage.setItem("discountCode", radio.value);
                localStorage.setItem("discountValue", radio.getAttribute("data-value"));
                localStorage.setItem("discountId", coupon.id);
                updateTotal(Number(radio.getAttribute("data-value")));
            }
        });
    });

    // Hiển thị chỉ 3 phiếu giảm giá tốt nhất còn lại
    let remainingCoupons = eligibleCoupons.slice(2, 4); // Lấy từ phiếu thứ 2 đến thứ 4

    remainingCoupons.forEach(coupon => {
        const couponElement = document.createElement('div');
        couponElement.classList.add('coupon');
        couponElement.innerHTML = `
            <div class="coupon-left">${coupon.giaTriGiam.toLocaleString()}</div>
            <div class="coupon-right">
                <div class="coupon-title">${coupon.tenPhieuGiamGia} - <strong>${coupon.maPhieuGiamGia}</strong></div>
                <div class="coupon-condition">Đơn hàng từ ${coupon.dieuKien.toLocaleString()} ${coupon.donViTinh}</div>
            </div>
            <input type="radio" name="coupon" class="coupon-select" value="${coupon.maPhieuGiamGia}"
                   data-value="${coupon.giaTriGiam}" ${coupon.maPhieuGiamGia === savedDiscountCode ? 'checked' : ''}>
        `;
        container.appendChild(couponElement);
    });

    // Nếu có phiếu giảm giá tự động chọn, cập nhật tổng
    if (selectedCoupon) {
        localStorage.setItem("discountCode", selectedCoupon.maPhieuGiamGia);
        localStorage.setItem("discountValue", selectedCoupon.giaTriGiam);
        localStorage.setItem("discountId", selectedCoupon.id);
        updateTotal(selectedCoupon.giaTriGiam);
    }
}

// Cập nhật danh sách phiếu khi giỏ hàng thay đổi
document.addEventListener("DOMContentLoaded", function () {
    renderCart();
    fetchCoupons();
});

document.getElementById("cart-body").addEventListener("click", function () {
    setTimeout(() => {
        fetchCoupons(); // Cập nhật phiếu khi thay đổi số lượng giỏ hàng
    }, 500);
});
