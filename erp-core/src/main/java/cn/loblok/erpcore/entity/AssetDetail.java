package cn.loblok.erpcore.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Data
@Table(name = "asset_detail", schema = "erp_test", catalog = "")
public class AssetDetail {
    /**
     * 主键
     */
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private long id;
    /**
     * 资产编号
     */
    @Basic
    @Column(name = "asset_code")
    private String assetCode;
    /**
     * 资产名称
     */
    @Basic
    @Column(name = "asset_name")
    private String assetName;
    /**
     * 资产类别编号
     */
    @Basic
    @Column(name = "category_code")
    private String categoryCode;
    /**
     * 资产类别名称
     */
    @Basic
    @Column(name = "category_name")
    private String categoryName;
    /**
     * 公司编号
     */
    @Basic
    @Column(name = "company_id")
    private int companyId;
    /**
     * 公司名称
     */
    @Basic
    @Column(name = "company_name")
    private String companyName;
    /**
     * 部门编号
     */
    @Basic
    @Column(name = "dept_id")
    private int deptId;
    /**
     * 部门名称
     */
    @Basic
    @Column(name = "dept_name")
    private String deptName;
    /**
     * 资产原值
     */
    @Basic
    @Column(name = "original_value")
    private BigDecimal originalValue;
    /**
     * 残值
     */
    @Basic
    @Column(name = "residual_value")
    private BigDecimal residualValue;
    /**
     * 折旧方式
     */
    @Basic
    @Column(name = "depreciation_method")
    private byte depreciationMethod;
    /**
     * 累计折旧
     */
    @Basic
    @Column(name = "accumulated_depreciation")
    private BigDecimal accumulatedDepreciation;
    /**
     * 采购日期
     */
    @Basic
    @Column(name = "purchase_date")
    private LocalDate purchaseDate;
    /**
     * 使用开始日期
     */
    @Basic
    @Column(name = "use_start_date")
    private LocalDate  useStartDate;
    /**
     * 预计使用期限（月）
     */
    @Basic
    @Column(name = "expected_life_months")
    private int expectedLifeMonths;
    /**
     * 状态
     */
    @Basic
    @Column(name = "status")
    private byte status;
    /**
     * 位置
     */
    @Basic
    @Column(name = "location")
    private String location;
    /**
     * 管理人
     */
    @Basic
    @Column(name = "custodian")
    private String custodian;
    /**
     * 供应商名称
     */
    @Basic
    @Column(name = "supplier_name")
    private String supplierName;
    /**
     * 合同编号
     */
    @Basic
    @Column(name = "contract_no")
    private String contractNo;
    /**
     * 创建时间
     */
    @Basic
    @Column(name = "created_at")
    private Timestamp createdAt;
    /**
     * 更新时间
     */
    @Basic
    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssetDetail that = (AssetDetail) o;
        return id == that.id && companyId == that.companyId && deptId == that.deptId && depreciationMethod == that.depreciationMethod && expectedLifeMonths == that.expectedLifeMonths && status == that.status && Objects.equals(assetCode, that.assetCode) && Objects.equals(assetName, that.assetName) && Objects.equals(categoryCode, that.categoryCode) && Objects.equals(categoryName, that.categoryName) && Objects.equals(companyName, that.companyName) && Objects.equals(deptName, that.deptName) && Objects.equals(originalValue, that.originalValue) && Objects.equals(residualValue, that.residualValue) && Objects.equals(accumulatedDepreciation, that.accumulatedDepreciation) && Objects.equals(purchaseDate, that.purchaseDate) && Objects.equals(useStartDate, that.useStartDate) && Objects.equals(location, that.location) && Objects.equals(custodian, that.custodian) && Objects.equals(supplierName, that.supplierName) && Objects.equals(contractNo, that.contractNo) && Objects.equals(createdAt, that.createdAt) && Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, assetCode, assetName, categoryCode, categoryName, companyId, companyName, deptId, deptName, originalValue, residualValue, depreciationMethod, accumulatedDepreciation, purchaseDate, useStartDate, expectedLifeMonths, status, location, custodian, supplierName, contractNo, createdAt, updatedAt);
    }
}