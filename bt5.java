CREATE TRIGGER TR_KIEMTRATRUNGTENVATTU
ON VATTU
FOR INSERT,UPDATE
AS
BEGIN
	--KIỂM TRA XUẤT HIỆN 2 LẦN>1
	IF (SELECT COUNT(A.TENVT) FROM VATTU A, inserted B WHERE A.TENVT=B.TENVT)>1
		BEGIN
			RAISERROR(N'TRÙNG TÊN VẬT TƯ',16,1)
			ROLLBACK TRANSACTION --HUỶ GIAO TÁC
		END
	ELSE
		PRINT N'THÊM THÀNH CÔNG'
END

INSERT VATTU VALUES('VT08',N'ĐINH','KG',25000,1000)

--2.Không cho phép CASCADE DELETE trong các ràng buộc khóa ngoại. 
--Ví dụ không cho phép xóa các HOADON nào có SOHD còn trong table CTHD.

CREATE TRIGGER TRG_KIEMTRA_CTHD
ON HOADON
FOR DELETE
AS
BEGIN
	DECLARE @MAHD VARCHAR(10)
	SET @MAHD=(SELECT MAHD FROM deleted)

	IF EXISTS(SELECT MAHD FROM CTHD WHERE MAHD=@MAHD)
		BEGIN
			RAISERROR(N'ĐÃ TỒN TẠI CHI TIẾT HOÁ ĐƠN',16,1)
			ROLLBACK TRANSACTION --HUỶ GIAO TÁC
		END
	ELSE
		PRINT N'XOÁ THÀNH CÔNG HOÁ ĐƠN HUỶ'
END

DELETE FROM HOADON WHERE MAHD='HD011'

--4. Khi user đặt hàng thì KHUYENMAI là 5% nếu SL > 100, 10% nếu SL > 500.

CREATE TRIGGER TRG_TUDONGCAPNHATKHUYENMAI
ON CTHD
FOR INSERT,UPDATE
AS
BEGIN
	UPDATE CTHD
	SET KHUYENMAI =IIF(SL>500,0.1*SL*GIABAN,IIF(SL>100,0.05*SL*GIABAN,0)) 
END

--TỰ ĐỘNG CẬP NHẬT TỔNG TRỊ GIÁ HOÁ ĐƠN

CREATE TRIGGER TRG_CAPNHATTONGTRIGIAHOADON
ON CTHD
FOR INSERT,UPDATE
AS
BEGIN
	UPDATE HOADON
	SET TONGTG=(SELECT SUM(SL*GIABAN) FROM CTHD WHERE CTHD.MAHD=HOADON.MAHD)
END

--XOÁ TRIGGER 
DROP TRIGGER TRG_CAPNHATTONGTRIGIAHOADON


--KIỂM TRA SỐ LƯỢNG BÁN (TRONG CTHD) KHÔNG ĐƯỢC VƯỢT QUÁ SỐ LƯỢNG TỒN (TRONG VATTU).
--CẬP NHẬT LẠI SỐ LƯỢNG TỒN KHO KHI ĐÃ BÁN
CREATE TRIGGER TRG_KIEMTRAVACAPNHAT_SOLUONGTONKHO
ON CTHD
FOR INSERT,UPDATE
AS
BEGIN
	DECLARE @MAVT VARCHAR(10),@SLBAN INT
	SELECT @MAVT=MAVT,@SLBAN=SL FROM inserted

	IF(@SLBAN >(SELECT SLTON FROM VATTU WHERE MAVT=@MAVT))
		BEGIN
			RAISERROR(N'KHÔNG ĐỦ HÀNG TRONG KHO',16,1)
			ROLLBACK TRANSACTION --HUỶ GIAO TÁC
		END
	ELSE
		UPDATE VATTU
		SET SLTON=SLTON-@SLBAN
		WHERE MAVT=@MAVT
END

--7.Mỗi hóa đơn cho phép bán tối đa 3 mặt hàng.
ALTER TRIGGER TRG_KIEMTRACTHD3MATHANG
ON CTHD
FOR INSERT,UPDATE
AS
BEGIN
	DECLARE @MAHD VARCHAR(10)
	SET @MAHD=(SELECT MAHD FROM inserted)

    IF(SELECT COUNT(MAVT) FROM CTHD WHERE MAHD=@MAHD)>3
		BEGIN
			RAISERROR(N'MỖI HOÁ ĐƠN CHỈ BÁN TỐI ĐA 3 MẶT HÀNG',16,1)
			ROLLBACK TRANSACTION --HUỶ GIAO TÁC
		END
END

