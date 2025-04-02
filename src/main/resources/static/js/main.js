(function ($) {
    "use strict";

    // Dropdown on mouse hover
    $(document).ready(function () {
        function toggleNavbarMethod() {
            if ($(window).width() > 992) {
                $('.navbar .dropdown').on('mouseover', function () {
                    $('.dropdown-toggle', this).trigger('click');
                }).on('mouseout', function () {
                    $('.dropdown-toggle', this).trigger('click').blur();
                });
            } else {
                $('.navbar .dropdown').off('mouseover').off('mouseout');
            }
        }
        toggleNavbarMethod();
        $(window).resize(toggleNavbarMethod);
    });


    // Back to top button
    $(window).scroll(function () {
        if ($(this).scrollTop() > 100) {
            $('.back-to-top').fadeIn('slow');
        } else {
            $('.back-to-top').fadeOut('slow');
        }
    });
    $('.back-to-top').click(function () {
        $('html, body').animate({scrollTop: 0}, 1500, 'easeInOutExpo');
        return false;
    });


    // Vendor carousel
    $('.vendor-carousel').owlCarousel({
        loop: true,
        margin: 29,
        nav: false,
        autoplay: true,
        smartSpeed: 1000,
        responsive: {
            0:{
                items:2
            },
            576:{
                items:3
            },
            768:{
                items:4
            },
            992:{
                items:5
            },
            1200:{
                items:6
            }
        }
    });


    // Related carousel
    $('.related-carousel').owlCarousel({
        loop: true,
        margin: 29,
        nav: false,
        autoplay: true,
        smartSpeed: 1000,
        responsive: {
            0:{
                items:1
            },
            576:{
                items:2
            },
            768:{
                items:3
            },
            992:{
                items:4
            }
        }
    });


    // Product Quantity
    $('.quantity button').on('click', function () {
        var button = $(this);
        var oldValue = button.parent().parent().find('input').val();
        if (button.hasClass('btn-plus')) {
            var newVal = parseFloat(oldValue) + 1;
        } else {
            if (oldValue > 0) {
                var newVal = parseFloat(oldValue) - 1;
            } else {
                newVal = 0;
            }
        }
        button.parent().parent().find('input').val(newVal);
    });

})(jQuery);

async function fetchProducts() {
    const apiUrl = `http://localhost:8080/api/sanpham?page=0`;  // Lấy sản phẩm từ API (không cần phân trang).

    try {
        const response = await fetch(apiUrl);
        if (!response.ok) {
            throw new Error(`Lỗi API! Mã trạng thái: ${response.status}`);
        }

        const data = await response.json();
        if (!data || !data.content) {
            throw new Error("API không trả về dữ liệu hợp lệ");
        }

        renderProducts(data.content); // Hiển thị sản phẩm lên trang
    } catch (error) {
        console.error("Lỗi khi tải sản phẩm:", error);
    }
}

function renderProducts(products) {
    const productListContainer = document.getElementById('product-list');
    productListContainer.innerHTML = ''; // Xóa sản phẩm cũ

    products.forEach(product => {
        const productElement = document.createElement('div');
        productElement.classList.add('col-lg-4', 'col-md-6', 'col-sm-12', 'pb-1');
        productElement.innerHTML = `
            <div class="card product-item border-0 mb-4" data-id="${product.id}">
                <div class="card-header product-img position-relative overflow-hidden bg-transparent border p-0">
                    <img class="img-fluid w-100" src="${product.urlHinhAnh || 'img/default-product.jpg'}" alt="${product.tenSanPham}">
                </div>
                <div class="card-body border-left border-right text-center p-0 pt-4 pb-3">
                    <h6 class="text-truncate mb-3">${product.tenSanPham}</h6>
                    <div class="d-flex justify-content-center">
                        <h6>${product.gia ? product.gia.toLocaleString('vi-VN') + 'đ' : '0đ'}</h6>
                    </div>
                </div>
                <div class="card-footer d-flex justify-content-between bg-light border">
                    <a href="#" class="btn btn-sm text-dark p-0 quick-view-btn text-center w-100" data-id="${product.id}">
                        <i class="fas fa-eye text-primary mr-1"></i>Xem nhanh
                    </a>
                </div>
            </div>
        `;
        productListContainer.appendChild(productElement);
    });

    // Xử lý sự kiện click vào toàn bộ sản phẩm
    document.querySelectorAll('.product-item').forEach(item => {
        item.addEventListener('click', function (event) {
            // Nếu bấm vào vùng "Xem nhanh", không cần xử lý tiếp
            if (event.target.closest('.quick-view-btn')) return;

            const productId = this.getAttribute('data-id');
            if (productId) {
                window.location.href = `/chi-tiet-san-pham?id=${productId}`;
            }
        });
    });

    // Xử lý click riêng cho nút "Xem nhanh"
    document.querySelectorAll('.quick-view-btn').forEach(button => {
        button.addEventListener('click', function (event) {
            event.preventDefault();
            const productId = this.getAttribute('data-id');
            if (productId) {
                window.location.href = `/chi-tiet-san-pham?id=${productId}`;
            }
        });
    });
}

// Khi trang được tải
document.addEventListener("DOMContentLoaded", function () {
    fetchProducts(); // Gọi API để lấy danh sách sản phẩm khi trang được tải
});


