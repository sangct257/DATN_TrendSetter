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
            if (product.quantity < product.availableQuantity) {
                product.quantity += 1;
            } else {
                Swal.fire({
                    icon: "warning",
                    title: "Không đủ hàng!",
                    text: `Chỉ còn ${product.availableQuantity - product.quantity} sản phẩm trong kho.`,
                    confirmButtonText: "OK"
                });
            }
        } else if (event.target.classList.contains("btn-minus")) {
            if (product.quantity > 1) {
                product.quantity -= 1;
            }
        } else if (event.target.classList.contains("btn-remove")) {
            cart = cart.filter(item => !(item.idSanPhamChiTiet == idSanPhamChiTiet && item.size == size && item.color == color));
        }

        localStorage.setItem("cart", JSON.stringify(cart));
        renderCart();
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
        <br> <small class="text-muted">Tồn kho: ${item.availableQuantity - item.quantity}</small>
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

    const maxVisibleCoupons = 3;
    let showAll = false;

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
            // 🚀 Reset về mặc định
            discountContainer.style.display = 'none';
            discountDisplay.innerText = "-0 VND";
        }
    }

    function updateView() {
        container.innerHTML = '';
        const displayedCoupons = showAll ? coupons : coupons.slice(0, maxVisibleCoupons);

        displayedCoupons.forEach(coupon => {
            let isEligible = cartTotal >= coupon.dieuKien;

            const couponElement = document.createElement('div');
            couponElement.classList.add('coupon');
            couponElement.innerHTML = `
            <div class="coupon-left">${coupon.giaTriGiam.toLocaleString()} ${coupon.donViTinh}</div>
            <div class="coupon-right">
                <div class="coupon-title">${coupon.tenPhieuGiamGia} - <strong>${coupon.maPhieuGiamGia}</strong></div>
                <div class="coupon-condition">Đơn hàng từ ${coupon.dieuKien.toLocaleString()} ${coupon.donViTinh}</div>
            </div>
            <input type="radio" name="coupon" class="coupon-select" value="${coupon.maPhieuGiamGia}" data-value="${coupon.giaTriGiam}" ${isEligible ? '' : 'disabled'}>
        `;
            container.appendChild(couponElement);

            // 🌟 Thêm sự kiện click để chọn/bỏ chọn radio, nhưng chặn nếu radio bị disabled
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

                if (event.target.classList.contains("coupon-select")) return; // Nếu bấm vào chính radio thì bỏ qua

                if (radio.checked) {
                    radio.checked = false;
                    localStorage.removeItem("discountCode");
                    localStorage.removeItem("discountValue");
                    localStorage.removeItem("discountId");
                    updateTotal(0);
                } else {
                    // Bỏ chọn các radio khác trước khi chọn cái mới
                    document.querySelectorAll(".coupon-select").forEach(r => r.checked = false);

                    radio.checked = true;
                    localStorage.setItem("discountCode", radio.value);
                    localStorage.setItem("discountValue", radio.getAttribute("data-value"));
                    localStorage.setItem("discountId", coupon.id);
                    updateTotal(Number(radio.getAttribute("data-value")));
                }
            });
        });

        // Xử lý nút "Xem thêm"
        if (coupons.length > maxVisibleCoupons) {
            const seeMoreButton = document.createElement('div');
            seeMoreButton.innerText = showAll ? "Thu gọn" : "Xem thêm";
            seeMoreButton.classList.add("see-more-btn");
            seeMoreButton.onclick = function () {
                showAll = !showAll;
                updateView();
            };
            container.appendChild(seeMoreButton);
        }
    }

    updateView();
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

