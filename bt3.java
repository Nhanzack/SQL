SELECT * FROM KHACHHANG
IF @@rowcount > 0
  BEGIN	
	PRINT N'Có dữ liệu'
	PRINT @@rowcount
  END
ELSE
	PRINT N'Bảng chưa có dữ liệu'

--KHAI BÁO BIẾN
DECLARE @TENVT NVARCHAR(10)
--GÁN GIÁ TRỊ CHO BIẾN
SET @TENVT=N'XI MĂNG'

IF EXISTS (SELECT TENVT FROM VATTU WHERE TENVT LIKE @TENVT +'%')
	SELECT * 
	FROM VATTU
	WHERE TENVT LIKE @TENVT +'%'
ELSE
	PRINT N'KHÔNG CÓ VẬT TƯ NÀY'

--CẤU TRÚC CASE...WHEN....ELSE....END
--16.Lấy ra các thông tin gồm mã hóa đơn, mã vật tư, 
--tên vật tư, đơn vị tính, giá bán, giá mua, số lượng, 
--trị giá mua (giá mua * số lượng), 
--trị giá bán (giá bán * số lượng) 
--và cột khuyến mãi với khuyến mãi 10% cho những mặt hàng bán trong một hóa đơn lớn hơn 100.

DECLARE @DIEM FLOAT
SET @DIEM=4

SELECT KETQUA=CASE WHEN @DIEM>=8.5 THEN N'GIỎI'
				   WHEN @DIEM>=6.5 THEN N'KHÁ'
				   WHEN @DIEM>=5 THEN N'TRUNG BÌNH'
				   ELSE N'YẾU'
			  END

SELECT *, QUY=CASE WHEN MONTH(NGAY) IN(1,2,3) THEN 1
			       WHEN MONTH(NGAY) IN(4,5,6) THEN 2
				   WHEN MONTH(NGAY) IN(7,8,9) THEN 3
				   ELSE 4
			  END
FROM HOADON

SELECT MAHD,A.MAVT,TENVT,DVT,SL,THANHTIEN=SL*GIABAN,
       KHUYENMAI=CASE WHEN SL>=1000 THEN 0.1*SL*GIABAN
	                  WHEN SL>=500 THEN 0.05*SL*GIABAN
					  ELSE 0.03*SL*GIABAN
                 END
FROM VATTU A,CTHD B
WHERE A.MAVT=B.MAVT

--STORE PROCEDURE CỦA SQL
SP_DETACH_DB QUANLYVATTU


--1.	Lấy ra danh các khách hàng đã mua hàng trong ngày X, với X là tham số truyền vào.
ALTER PROC P1(@X DATE)
AS
BEGIN

	IF EXISTS (SELECT NGAY FROM HOADON WHERE NGAY=@X)
		SELECT * 
		FROM KHACHHANG A,HOADON B
		WHERE NGAY=@X
	ELSE
		PRINT N'KHÔNG CÓ KHÁCH HANG MUA TRONG NGAY '+ CAST(@X  AS VARCHAR(20))
END

P1 '12/25/2010'

--2.Lấy ra danh sách khách hàng có tổng trị giá các đơn hàng lớn hơn X 
--(X là tham số).
CREATE PROC P2(@X INT)
AS
BEGIN
	SELECT A.MAKH,TENKH,TONGGTDONHANG=SUM(SL*GIABAN)
	FROM KHACHHANG A,HOADON B,CTHD C
	WHERE A.MAKH=B.MAKH AND B.MAHD=C.MAHD
	GROUP BY A.MAKH,TENKH
	HAVING SUM(SL*GIABAN)>@X
END

EXEC P2 5000000

--3.Lấy ra danh sách X khách hàng có tổng trị giá các đơn hàng lớn nhất 
--(X là tham số).

CREATE PROC P3(@X TINYINT)
AS
BEGIN
   IF(@X<=0)
     PRINT N'SAI DỮ LIỆU'
   ELSE
	SELECT TOP(@X) WITH TIES A.MAHD,NGAY,TENKH,TONGGTDONHANG=SUM(SL*GIABAN)
	FROM HOADON A,KHACHHANG B,CTHD C
	WHERE A.MAKH=B.MAKH AND A.MAHD=C.MAHD
	GROUP BY A.MAHD,NGAY,TENKH
	ORDER BY TONGGTDONHANG DESC
END
P3 3

--TÌM X MẶT HÀNG ĐƯỢC BÁN NHIỀU LẦN (BÁN CHẠY) NHẤT
CREATE PROC P3A(@X TINYINT)
AS
BEGIN
   IF(@X<=0)
     PRINT N'SAI DỮ LIỆU'
   ELSE
	SELECT TOP(@X) WITH TIES A.MAVT,TENVT,SOLANBAN=COUNT(MAHD)
	FROM VATTU A,CTHD B
	WHERE A.MAVT=B.MAVT
	GROUP BY A.MAVT,TENVT
	ORDER BY SOLANBAN DESC
END

--3B. 
--7.Tính giá trị cho cột khuyến mãi như sau: Khuyến mãi 5% nếu SL > 100,
--10% nếu SL > 500.

CREATE PROC P7
AS
BEGIN
	UPDATE CTHD
	SET KHUYENMAI=CASE WHEN SL>500 THEN 0.1*SL*GIABAN
					   WHEN SL>100 THEN 0.05*SL*GIABAN
                       ELSE	0
				  END
END
P7

SELECT * FROM CTHD

CREATE PROC P8(@MAVT VARCHAR(20))
AS
BEGIN
	UPDATE VATTU
	SET SLTON=SLTON -(SELECT SUM(SL) FROM CTHD WHERE MAVT=@MAVT)
END
P8 'VT02'

--9.Tính trị giá cho mỗi hóa đơn.
CREATE PROC P9
AS
BEGIN
	UPDATE HOADON
	SET TONGTG=(SELECT SUM(SL*GIABAN) FROM CTHD WHERE MAHD=HOADON.MAHD)
END


--10.Tạo ra table KH_VIP có cấu trúc giống với cấu trúc table KHACHHANG. 
--Lưu các khách hàng có tổng trị giá của tất cả các đơn hàng >=10,000,000 
--vào table KH_VIP.

ALTER PROC P10
AS
BEGIN
	SELECT A.MAKH,TENKH,DIACHI,DT,EMAIL
	INTO #KH_VIP
	FROM KHACHHANG A,HOADON B
	WHERE A.MAKH=B.MAKH
	GROUP BY A.MAKH,TENKH,DIACHI,DT,EMAIL
	HAVING SUM(TONGTG)>=10000000

	SELECT * FROM #KH_VIP

	DROP TABLE #KH_VIP
END




    }
}


