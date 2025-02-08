var app = angular.module('myApp', ['ngRoute']);

app.config(['$routeProvider', function ($routeProvider) {
    $routeProvider
        .when('/san-pham/hien-thi', {
            templateUrl: '/san-pham/hien-thi',
            controller: 'SanPhamController'
        })

        .when('/san-pham/:sanPhamId', {
            templateUrl: function(params) {
                return '/san-pham/' + params.sanPhamId;  // Truyền tham số sanPhamId vào URL
            },
            controller: 'ChiTietSanPhamController'
        })

        .when('/hoa-don/hien-thi', {
            templateUrl: '/hoa-don/hien-thi',
            controller: 'HoaDonController'
        })

        .when('/hoa-don-chi-tiet/hien-thi', {
            templateUrl: '/hoa-don-chi-tiet/hien-thi',
            controller: 'HoaDonChiTietController'
        })

        .when('/nhan-vien/hien-thi', {
            templateUrl: '/nhan-vien/hien-thi',
            controller: 'NhanVienController'
        })

        .when('/khach-hang/hien-thi', {
            templateUrl: '/khach-hang/hien-thi',
            controller: 'KhachHangController'
        })

        .when('/chat-lieu/hien-thi', {
            templateUrl: '/chat-lieu/hien-thi',
            controller: 'ChatLieuController'
        })

        .when('/danh-gia/hien-thi', {
            templateUrl: '/danh-gia/hien-thi',
            controller: 'DanhGiaController'
        })

        .when('/danh-muc/hien-thi', {
            templateUrl: '/danh-muc/hien-thi',
            controller: 'DanhMucController'
        })

        .when('/dia-chi/hien-thi', {
            templateUrl: '/dia-chi/hien-thi',
            controller: 'DiaChiController'
        })

        .when('/hinh-anh/hien-thi', {
            templateUrl: '/hinh-anh/hien-thi',
            controller: 'HinhAnhController'
        })

        .when('/phieu-giam-gia/hien-thi', {
            templateUrl: '/phieu-giam-gia/hien-thi',
            controller: 'PhieuGiamGiaController'
        })

        .when('/dot-giam-gia/hien-thi', {
            templateUrl: '/dot-giam-gia/hien-thi',
            controller: 'DotGiamGiaController'
        })

        .when('/mau-sac/hien-thi', {
            templateUrl: '/mau-sac/hien-thi',
            controller: 'MauSacController'
        })

        .when('/phuong-thuc-thanh-toan/hien-thi', {
            templateUrl: '/phuong-thuc-thanh-toan/hien-thi',
            controller: 'PhuongThucThanhToanController'
        })

        .when('/thuong-hieu/hien-thi', {
            templateUrl: '/thuong-hieu/hien-thi',
            controller: 'ThuongHieuController'
        })

        .when('/xuat-su/hien-thi', {
            templateUrl: '/xuat-su/hien-thi',
            controller: 'XuatSuController'
        })

        .when('/xuat-su/update/:id', {
            templateUrl: '/xuat-su/update',  // Thêm URL cần thiết
            controller: 'XuatSuUpdateController'
        })


        .when('/kich-thuoc/hien-thi', {
            templateUrl: '/kich-thuoc/hien-thi',
            controller: 'KichThuocController'
        })

        .when('/sell-counter', {
            templateUrl: '/sell-counter',
            controller: 'SellCounterController'
        })

        .otherwise({
            redirectTo: ''
        });
}]);

app.controller('SanPhamController', ['$scope', function ($scope) {
    $scope.message = "Đây là trang Quản Lý Sản Phẩm.";
}]);

app.controller('ChiTietSanPhamController', ['$scope', '$routeParams', function ($scope, $routeParams) {
    $scope.sanPhamId = $routeParams.sanPhamId;
    // Logic xử lý chi tiết sản phẩm
    $scope.message = "Chi tiết sản phẩm với ID: " + $scope.sanPhamId;
}]);

app.controller('HoaDonController', ['$scope', function ($scope) {
    $scope.message = "Đây là trang Quản Lý Hóa Đơn.";
}]);

app.controller('HoaDonChiTietController', ['$scope', function ($scope) {
    $scope.message = "Đây là trang Quản Lý Hóa Đơn Chi Tiết.";
}]);

app.controller('NhanVienController', ['$scope', function ($scope) {
    $scope.message = "Đây là trang Quản Lý Nhân Viên.";
}]);

app.controller('KhachHangController', ['$scope', function ($scope) {
    $scope.message = "Đây là trang Quản Lý Khách Hàng.";
}]);

app.controller('ChatLieuController', ['$scope', function ($scope) {
    $scope.message = "Đây là trang Quản Lý Chất Liệu.";
}]);

app.controller('DanhGiaController', ['$scope', function ($scope) {
    $scope.message = "Đây là trang Quản Lý Đánh Giá.";
}]);

app.controller('DanhMucController', ['$scope', function ($scope) {
    $scope.message = "Đây là trang Quản Lý Danh Mục.";
}]);

app.controller('DiaChiController', ['$scope', function ($scope) {
    $scope.message = "Đây là trang Quản Lý Địa Chỉ.";
}]);

app.controller('HinhAnhController', ['$scope', function ($scope) {
    $scope.message = "Đây là trang Quản Lý Hình Ảnh.";
}]);

app.controller('PhieuGiamGiaController', ['$scope', function ($scope) {
    $scope.message = "Đây là trang Quản Lý Phiếu Giảm Giá.";
}]);

app.controller('DotGiamGiaController', ['$scope', function ($scope) {
    $scope.message = "Đây là trang Quản Lý Đợt Giảm Giá.";
}]);

app.controller('MauSacController', ['$scope', function ($scope) {
    $scope.message = "Đây là trang Quản Lý Màu Sắc.";
}]);

app.controller('PhuongThucThanhToanController', ['$scope', function ($scope) {
    $scope.message = "Đây là trang Quản Lý Phương Thức Thanh Toán.";
}]);

app.controller('ThuongHieuController', ['$scope', function ($scope) {
    $scope.message = "Đây là trang Quản Lý Thương Hiệu.";
}]);

app.controller('XuatSuController', ['$scope', function ($scope) {
    $scope.message = "Đây là trang Quản Lý Xuất Xứ.";
}]);

app.controller('KichThuocController', ['$scope', function ($scope) {
    $scope.message = "Đây là trang Quản Lý Kích Thước.";
}]);

app.controller('SellCounterController', ['$scope', function ($scope) {
    $scope.message = "Đây là trang Bán Hàng.";
}]);

app.controller('XuatSuController', ['$scope', '$http', function($scope, $http) {
    // Hàm updateDanhMuc sẽ được gọi khi form được submit
    $scope.updateDanhMuc = function(danhMuc) {
        // Tạo một đối tượng chứa dữ liệu cần cập nhật
        const data = {
            id: danhMuc.id,
            tenDanhMuc: danhMuc.tenDanhMuc
        };

        // Gửi yêu cầu POST tới controller Spring Boot
        $http.post('/danh-muc/update', data).then(function(response) {
            if (response.data.success) {
                // Nếu cập nhật thành công, thông báo cho người dùng
                alert('Cập nhật danh mục thành công!');
            } else {
                // Nếu có lỗi
                alert('Lỗi khi cập nhật danh mục!');
            }
        }, function(error) {
            // Xử lý lỗi nếu có
            console.error('Error:', error);
            alert('Đã có lỗi xảy ra');
        });
    };
}]);



