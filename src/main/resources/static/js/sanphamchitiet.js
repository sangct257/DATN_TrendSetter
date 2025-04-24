let productList = []; // Lưu toàn bộ danh sách sản phẩm
let selectedSize = null;
let selectedColor = null;

// 🚀 Hàm lấy chi tiết sản phẩm
async function fetchProductDetails(productId) {
    try {
        const response = await fetch(`http://localhost:8080/api/san-pham-chi-tiet/${productId}`);
        if (!response.ok) {
            throw new Error(`Lỗi API! Mã trạng thái: ${response.status}`);
        }
        productList = await response.json();

        console.log(productList);  // Kiểm tra dữ liệu trả về từ API
        if (!Array.isArray(productList) || productList.length === 0) {
            throw new Error("Dữ liệu sản phẩm không hợp lệ hoặc rỗng!");
        }

        updateProductDetails(productList);

        // 🚀 Tự động chọn màu sắc và size đầu tiên có sẵn
        const availableColor = productList.find(p => p.soLuongTheoSize && Object.values(p.soLuongTheoSize).some(q => q > 0));
        if (availableColor) {
            const firstAvailableColor = availableColor.tenMauSac;
            const firstColorElement = document.querySelector(`.color-swatch[data-color="${firstAvailableColor}"]`);
            if (firstColorElement) {
                selectColor(firstColorElement, firstAvailableColor);
            }

            const availableSizes = Object.keys(availableColor.soLuongTheoSize).filter(size => availableColor.soLuongTheoSize[size] > 0);
            if (availableSizes.length > 0) {
                const firstAvailableSize = availableSizes[0];
                const firstSizeElement = document.querySelector(`.size-swatch[data-size="${firstAvailableSize}"]`);
                if (firstSizeElement) {
                    selectSize(firstSizeElement, firstAvailableSize);
                }
            }
        }
    } catch (error) {
        console.error('Lỗi khi tải sản phẩm:', error);
    }
}

// 🚀 Cập nhật giao diện sản phẩm
function updateProductDetails(products) {
    const firstProduct = products[0];

    document.querySelector('.product-title').textContent = firstProduct.tenSanPham || "Không có tên sản phẩm";
    // Sử dụng innerHTML để giữ lại các thẻ <br/>
    document.querySelector('.product-description').innerHTML = firstProduct.moTa || "Mô tả chưa cập nhật";
    document.querySelector('.product-chatLieu').textContent = firstProduct.tenChatLieu || "Chất liệu chưa cập nhật";
    document.querySelector('.product-xuatXu').textContent = firstProduct.quocGia || "Xuất xứ chưa cập nhật";
    document.querySelector('.product-thuongHieu').textContent = firstProduct.tenThuongHieu || "Thương hiệu chưa cập nhật";

    // Tính tổng số lượng sản phẩm từ soLuongTheoSize
    const totalQuantity = Object.values(firstProduct.soLuongTheoSize).reduce((sum, quantity) => sum + quantity, 0);

    // Hiển thị số lượng tổng cộng
    document.querySelector('.product-soLuong').textContent = totalQuantity > 0 ? `Số lượng còn lại: ${totalQuantity}` : "Số lượng còn lại chưa cập nhật";

    // 🚀 Hiển thị số lượng theo từng size
    const sizeQuantityText = Object.entries(firstProduct.soLuongTheoSize).map(([size, quantity]) => {
        return `${size}: ${quantity}`;
    }).join(', ');

    const allImages = [...new Set(products.flatMap(p => p.hinhAnh))];
    document.getElementById('mainImage').src = allImages[0] || "https://via.placeholder.com/300";
    document.querySelector('.thumbnails').innerHTML = allImages.map((img, index) => `
        <img src="${img}" alt="Thumbnail ${index}" onclick="changeImage('${img}')">
    `).join('');

    // 🚀 Hiển thị màu sắc
    const colors = [...new Set(products.map(p => p.tenMauSac))];
    let firstAvailableColor = colors[0];

    const colorOptions = document.getElementById('color-options');
    colorOptions.innerHTML = colors.map((color) => {
        const availableProducts = products.filter(p => p.tenMauSac === color);
        const available = availableProducts.some(p => p.soLuongTheoSize && Object.values(p.soLuongTheoSize).some(q => q > 0));
        return `
            <div class="swatch-1-s color-swatch ${!available ? 'disabled' : ''}" data-color="${color}">${color}</div>
        `;
    }).join('');

    document.querySelectorAll('.color-swatch').forEach(el => {
        el.addEventListener('click', function () {
            if (!el.classList.contains('disabled')) {
                selectColor(this, this.dataset.color);
            }
        });
    });

    // Nếu có màu sắc hợp lệ, chọn màu đầu tiên
    if (firstAvailableColor) {
        const firstColorElement = document.querySelector(`.color-swatch[data-color="${firstAvailableColor}"]`);
        if (firstColorElement && !firstColorElement.classList.contains('disabled')) {
            selectColor(firstColorElement, firstAvailableColor);
        }
    }

    // 🚀 Hiển thị tất cả size hợp lệ
    const sizes = [...new Set(products.flatMap(p => p.sizes))];
    const sizeOptions = document.getElementById('size-options');
    sizeOptions.innerHTML = sizes.map((size) => {
        const availableProducts = products.filter(p => p.sizes.includes(size));
        const available = availableProducts.some(p => p.soLuongTheoSize && p.soLuongTheoSize[size] > 0);
        return `
            <div class="swatch-1-s size-swatch ${!available ? 'disabled' : ''}" data-size="${size}">${size}</div>
        `;
    }).join('');

    document.querySelectorAll('.size-swatch').forEach(el => {
        el.addEventListener('click', function () {
            if (!el.classList.contains('disabled')) {
                selectSize(this, this.dataset.size);
            }
        });
    });

    selectedColor = null;
    selectedSize = null;

    updatePrice();
}

// 🚀 Chọn màu sắc
function selectColor(element, color) {
    document.querySelectorAll('.color-swatch').forEach(el => el.classList.remove('selected'));
    element.classList.add('selected');
    selectedColor = color;

    // Lọc sản phẩm theo màu sắc đã chọn
    const availableProducts = productList.filter(p => p.tenMauSac === selectedColor);

    // Cập nhật size hợp lệ cho màu sắc đã chọn
    let firstAvailableSize = null;
    document.querySelectorAll('.size-swatch').forEach(el => {
        const size = el.dataset.size;
        const available = availableProducts.some(p => p.sizes.includes(size) && p.soLuongTheoSize[size] > 0);
        if (available) {
            el.classList.remove('disabled');
            if (!firstAvailableSize) firstAvailableSize = size;
        } else {
            el.classList.add('disabled');
        }
    });

    // Nếu size đã chọn không hợp lệ, chọn lại size đầu tiên hợp lệ
    if (!firstAvailableSize) {
        selectedSize = null; // Không chọn size nếu không có size hợp lệ
    } else {
        selectedSize = firstAvailableSize;
    }

    // Cập nhật số lượng theo size và màu sắc đã chọn
    updateQuantityAndPrice();

    // Cập nhật hình ảnh sản phẩm
    updateProductImages(availableProducts);

    updatePrice();
}

// 🚀 Cập nhật hình ảnh sản phẩm khi thay đổi màu sắc
function updateProductImages(products) {
    // Lọc các sản phẩm có màu sắc đã chọn
    const selectedColorProducts = products.filter(product => product.tenMauSac === selectedColor);

    // Lấy tất cả các hình ảnh của sản phẩm có màu sắc đã chọn và loại bỏ trùng lặp
    const uniqueImages = [];
    selectedColorProducts.forEach(product => {
        product.hinhAnh.forEach(image => {
            if (!uniqueImages.includes(image)) {
                uniqueImages.push(image);
            }
        });
    });

    // Nếu có hình ảnh, hiển thị ảnh đầu tiên làm ảnh chính
    if (uniqueImages.length > 0) {
        const mainImage = document.getElementById('mainImage');
        // Giữ ảnh chính là ảnh đầu tiên của uniqueImages
        if (mainImage.src !== uniqueImages[0]) {
            mainImage.src = uniqueImages[0];
        }
    } else {
        document.getElementById('mainImage').src = "https://via.placeholder.com/300"; // Nếu không có hình ảnh, hiển thị ảnh mặc định
    }

    // Hiển thị các ảnh thumbnail
    document.querySelector('.thumbnails').innerHTML = uniqueImages.map((img, index) => `
        <img src="${img}" alt="Thumbnail ${index}" onclick="changeImage('${img}')">
    `).join('');
}

// 🚀 Chọn size
function selectSize(element, size) {
    document.querySelectorAll('.size-swatch').forEach(el => el.classList.remove('selected'));
    element.classList.add('selected');
    selectedSize = size;

    // Cập nhật số lượng và giá theo size và màu sắc đã chọn
    updateQuantityAndPrice();

    updatePrice();
}

// 🚀 Cập nhật số lượng và giá khi chọn size và màu sắc
function updateQuantityAndPrice() {
    // Tìm sản phẩm phù hợp với màu sắc và size đã chọn
    const selectedProduct = productList.find(p => p.tenMauSac === selectedColor && p.sizes.includes(selectedSize));

    if (selectedProduct) {
        // Lấy số lượng của size đã chọn
        const availableQuantity = selectedProduct.soLuongTheoSize[selectedSize] || 0;

        // Hiển thị số lượng còn lại trên giao diện
        document.querySelector('.product-soLuong').textContent = availableQuantity > 0 ?
            `Số lượng còn lại: ${availableQuantity}` : "Hết hàng";
    }
}

// 🚀 Cập nhật giá theo lựa chọn
function updatePrice() {
    const selectedProduct = productList.find(p => p.tenMauSac === selectedColor && p.sizes.includes(selectedSize));

    if (selectedProduct && selectedProduct.soLuongTheoSize[selectedSize] > 0) {
        document.querySelector('.product-price').textContent =
            `${selectedProduct.gia.toLocaleString('vi-VN')} VNĐ`;
    } else {
        document.querySelector('.product-price').textContent = "Hết hàng";
    }
}


// 🚀 Thay đổi ảnh chính khi bấm ảnh nhỏ
function changeImage(imageSrc) {
    document.getElementById('mainImage').src = imageSrc;
}

// 🚀 Xử lý số lượng sản phẩm
function increaseQuantity() {
    let quantityInput = document.getElementById('quantity');
    let currentQuantity = parseInt(quantityInput.value);

    // Tìm sản phẩm đang chọn
    const selectedProduct = productList.find(p => p.tenMauSac === selectedColor && p.sizes.includes(selectedSize));

    if (!selectedProduct) return;

    const availableQuantity = selectedProduct.soLuongTheoSize[selectedSize] || 0;

    // Kiểm tra số lượng tối đa là 20
    if (currentQuantity < 20) {
        if (currentQuantity < availableQuantity) {
            quantityInput.value = currentQuantity + 1;
        } else {
            Swal.fire({
                icon: "warning",
                title: "Vượt quá số lượng tồn kho!",
                text: `Chỉ còn lại ${availableQuantity} sản phẩm.`,
                confirmButtonText: "OK"
            });
        }
    } else {
        Swal.fire({
            icon: "warning",
            title: "Số lượng tối đa là 20!",
            confirmButtonText: "OK"
        });
    }
}


function decreaseQuantity() {
    let quantityInput = document.getElementById('quantity');
    if (parseInt(quantityInput.value) > 1) {
        quantityInput.value = parseInt(quantityInput.value) - 1;
    }
}

// 🚀 Chạy khi trang tải xong
document.addEventListener("DOMContentLoaded", () => {
    const productId = new URLSearchParams(window.location.search).get('id');
    if (productId) {
        fetchProductDetails(productId);
    }
});

// 🔹 Hàm cập nhật số lượng trên giỏ hàng
function updateCartBadge() {
    let cart = JSON.parse(localStorage.getItem("cart")) || [];
    let totalQuantity = cart.reduce((sum, item) => sum + item.quantity, 0);

    // Cập nhật số trên badge
    document.querySelector(".badge").textContent = totalQuantity;
}

// 🔹 Gọi hàm cập nhật ngay khi tải trang
document.addEventListener("DOMContentLoaded", updateCartBadge);
// 🔹 Thêm vào giỏ hàng
// 🔹 Thêm vào giỏ hàng
document.querySelector(".buy-button").addEventListener("click", function (event) {
    event.preventDefault();

    if (!selectedColor || !selectedSize) {
        Swal.fire({
            icon: "warning",
            title: "Vui lòng chọn màu sắc và size!",
            text: "Bạn cần chọn đầy đủ trước khi thêm vào giỏ hàng.",
            confirmButtonText: "OK"
        });
        return;
    }

    const selectedProduct = productList.find(p => p.tenMauSac === selectedColor && p.sizes.includes(selectedSize));

    if (!selectedProduct) {
        Swal.fire({
            icon: "error",
            title: "Sản phẩm không hợp lệ!",
            text: "Không tìm thấy sản phẩm phù hợp với lựa chọn của bạn.",
            confirmButtonText: "OK"
        });
        return;
    }

    // ✅ Lấy ID sản phẩm chi tiết thay vì ID sản phẩm chung
    const idSanPhamChiTiet = selectedProduct.idSanPhamChiTiet;
    const availableQuantity = selectedProduct.soLuongTheoSize[selectedSize] || 0;

    let quantityInput = document.getElementById('quantity');
    let quantity = parseInt(quantityInput.value);

    if (isNaN(quantity) || quantity <= 0) {
        Swal.fire({
            icon: "warning",
            title: "Số lượng không hợp lệ!",
            text: "Vui lòng nhập số lượng hợp lệ.",
            confirmButtonText: "OK"
        });
        return;
    }

    if (quantity > availableQuantity) {
        Swal.fire({
            icon: "warning",
            title: "Không đủ hàng!",
            text: `Chỉ còn ${availableQuantity} sản phẩm trong kho.`,
            confirmButtonText: "OK"
        });
        return;
    }

    let cart = JSON.parse(localStorage.getItem("cart")) || [];
    let existingProduct = cart.find(item => item.idSanPhamChiTiet === idSanPhamChiTiet);

    if (existingProduct) {
        if (existingProduct.quantity + quantity > availableQuantity) {
            Swal.fire({
                icon: "warning",
                title: "Không thể thêm vào giỏ hàng!",
                html: `
                    <p><strong>Bạn đã có ${existingProduct.quantity} sản phẩm trong giỏ.</strong></p>
                    <p>Số lượng yêu cầu <strong>vượt quá số lượng tồn kho</strong> (<strong>${availableQuantity}</strong>).</p>
                    <p>⚠️ Mong quý khách thông cảm và vui lòng điều chỉnh lại số lượng!</p>
                `,
                confirmButtonText: "OK"
            });
            return;
        }
        existingProduct.quantity += quantity;
    } else {
        cart.push({
            idSanPhamChiTiet: idSanPhamChiTiet,
            name: selectedProduct.tenSanPham,
            price: selectedProduct.gia.toLocaleString('vi-VN'),
            size: selectedSize,
            color: selectedColor,
            image: document.getElementById('mainImage').src,
            quantity: quantity,
            availableQuantity: availableQuantity
        });
    }

    localStorage.setItem("cart", JSON.stringify(cart));
    updateCartBadge();

    Swal.fire({
        icon: "success",
        title: "Thêm vào giỏ hàng thành công!",
        text: "Sản phẩm đã được thêm vào giỏ hàng.",
        confirmButtonText: "Tiếp tục mua sắm"
    });

    console.log("Sản phẩm đã thêm vào giỏ hàng:", {
        idSanPhamChiTiet: idSanPhamChiTiet,
        name: selectedProduct.tenSanPham,
        price: selectedProduct.gia,
        size: selectedSize,
        color: selectedColor,
        image: document.getElementById('mainImage').src,
        quantity: existingProduct ? existingProduct.quantity : quantity
    });

    console.log("Giỏ hàng hiện tại:", JSON.parse(localStorage.getItem("cart")));
});

// 🔹 Xử lý nút Mua ngay
document.querySelector(".buy-now-button").addEventListener("click", function (event) {
    event.preventDefault();

    if (!selectedColor || !selectedSize) {
        Swal.fire({
            icon: "warning",
            title: "Vui lòng chọn màu sắc và size!",
            text: "Bạn cần chọn đầy đủ trước khi mua.",
            confirmButtonText: "OK"
        });
        return;
    }

    const selectedProduct = productList.find(p => p.tenMauSac === selectedColor && p.sizes.includes(selectedSize));
    if (!selectedProduct) {
        Swal.fire({
            icon: "error",
            title: "Sản phẩm không hợp lệ!",
            text: "Không tìm thấy sản phẩm phù hợp với lựa chọn của bạn.",
            confirmButtonText: "OK"
        });
        return;
    }

    const idSanPhamChiTiet = selectedProduct.idSanPhamChiTiet;
    const availableQuantity = selectedProduct.soLuongTheoSize[selectedSize] || 0;

    let quantityInput = document.getElementById('quantity');
    let quantity = parseInt(quantityInput.value);

    if (isNaN(quantity) || quantity <= 0) {
        Swal.fire({
            icon: "warning",
            title: "Số lượng không hợp lệ!",
            text: "Vui lòng nhập số lượng hợp lệ.",
            confirmButtonText: "OK"
        });
        return;
    }

    if (quantity > availableQuantity) {
        Swal.fire({
            icon: "warning",
            title: "Không đủ hàng!",
            text: `Chỉ còn ${availableQuantity} sản phẩm trong kho.`,
            confirmButtonText: "OK"
        });
        return;
    }

    let cart = JSON.parse(localStorage.getItem("cart")) || [];
    let existingProduct = cart.find(item => item.idSanPhamChiTiet === idSanPhamChiTiet);

    if (existingProduct) {
        if (existingProduct.quantity + quantity > availableQuantity) {
            Swal.fire({
                icon: "warning",
                title: "Không thể mua ngay!",
                html: `
                    <p><strong>Bạn đã có ${existingProduct.quantity} sản phẩm trong giỏ.</strong></p>
                    <p>Số lượng yêu cầu <strong>vượt quá số lượng tồn kho</strong> (<strong>${availableQuantity}</strong>).</p>
                `,
                confirmButtonText: "OK"
            });
            return;
        }
        existingProduct.quantity += quantity;
    } else {
        cart.push({
            idSanPhamChiTiet: idSanPhamChiTiet,
            name: selectedProduct.tenSanPham,
            price: selectedProduct.gia.toLocaleString('vi-VN'),
            size: selectedSize,
            color: selectedColor,
            image: document.getElementById('mainImage').src,
            quantity: quantity,
            availableQuantity: availableQuantity
        });
    }

    localStorage.setItem("cart", JSON.stringify(cart));
    updateCartBadge();

    // ✅ Sau khi thêm vào giỏ, chuyển hướng tới trang giỏ hàng
    window.location.href = "http://localhost:8080/gio-hang";
});

function openSizeGuide() {
    document.getElementById('sizeGuidePopup').style.display = 'flex';
    document.body.classList.add('no-scroll'); // Chặn cuộn trang
}

function closeSizeGuide(event) {
    let modal = document.getElementById('sizeGuidePopup');
    if (event.target === modal || event.target.classList.contains('close-btn')) {
        modal.style.display = 'none';
        document.body.classList.remove('no-scroll'); // Bật lại cuộn trang
    }
}

document.addEventListener("DOMContentLoaded", function () {
    fetch("http://localhost:8080/api/danh-muc")
        .then(response => response.json())
        .then(data => {
            let danhMucContainer = document.getElementById("danh-muc-list");
            danhMucContainer.innerHTML = ""; // Xóa danh mục cũ

            data.forEach(danhMuc => {
                let danhMucElement = document.createElement("a");
                danhMucElement.href = "/san-pham"; // Chuyển hướng tới trang sản phẩm
                danhMucElement.className = "nav-item nav-link";
                danhMucElement.textContent = danhMuc.tenDanhMuc;

                // Khi nhấn vào danh mục, lưu giá trị vào sessionStorage
                danhMucElement.addEventListener("click", function (event) {
                    event.preventDefault(); // Ngăn chặn hành động mặc định

                    sessionStorage.setItem("selectedCategory", danhMuc.tenDanhMuc); // Lưu danh mục vào sessionStorage

                    window.location.href = "/san-pham"; // Chuyển hướng tới trang sản phẩm
                });

                danhMucContainer.appendChild(danhMucElement);
            });
        })
        .catch(error => console.error("Lỗi tải danh mục:", error));
});

// Hàm mở/đóng Accordion khi click
function toggleAccordion() {
    const accordionButton = document.querySelector('.accordion-button');
    const accordionContent = document.getElementById('productInfo');

    // Kiểm tra nếu accordion đang mở
    if (accordionContent.classList.contains('collapse')) {
        // Mở accordion
        accordionContent.classList.remove('collapse');
        accordionContent.classList.add('show');
        accordionButton.classList.add('open'); // Thêm class open cho button
    } else {
        // Đóng accordion
        accordionContent.classList.remove('show');
        accordionContent.classList.add('collapse');
        accordionButton.classList.remove('open'); // Bỏ class open cho button
    }
}

// Thêm sự kiện cho nút accordion
document.querySelector('.accordion-button').addEventListener('click', toggleAccordion);


