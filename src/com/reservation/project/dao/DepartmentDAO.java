package com.reservation.project.dao;

import com.reservation.project.model.Department;
import com.reservation.project.util.SqlUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


/**
 * DepartmentDAO类，用于处理部门相关的数据库操作
 * 包括查询所有部门、添加部门、更新部门和删除部门等功能
 */
public class DepartmentDAO {

    /**
     * 查询所有部门信息
     * @return 返回包含所有部门信息的列表，如果没有部门则返回空列表
     */
    public List<Department> listAll() {
        // 初始化部门列表
        List<Department> list = new ArrayList<Department>();
        // 定义SQL查询语句，按部门ID排序
        String sql = "SELECT dept_id, dept_name FROM department ORDER BY dept_id";
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            // 获取数据库连接
            con = SqlUtil.getConnection();
            if (con == null) {
                return list;
            }
            // 创建预处理语句
            ps = con.prepareStatement(sql);
            // 执行查询
            rs = ps.executeQuery();
            // 遍历结果集，将每个部门信息添加到列表中
            while (rs.next()) {
                Department d = new Department();
                d.setDeptId(rs.getLong("dept_id"));
                d.setDeptName(rs.getString("dept_name"));
                list.add(d);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭所有资源
            SqlUtil.closeAll(con, ps, rs);
        }
        return list;
    }


    /**
     * 添加新部门
     * @param deptName 部门名称
     * @return 添加成功返回true，如果部门名称已存在或其他错误则返回false
     */
    public boolean addDepartment(String deptName) {
        // 定义SQL插入语句
        String sql = "INSERT INTO department(dept_name) VALUES(?)";
        Connection con = null;
        PreparedStatement ps = null;
        try {
            // 获取数据库连接
            con = SqlUtil.getConnection();
            if (con == null) {
                return false;
            }
            // 创建预处理语句
            ps = con.prepareStatement(sql);
            // 设置参数
            ps.setString(1, deptName);
            // 执行更新并返回结果
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            return false; // 重名
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            // 关闭资源
            SqlUtil.closeAll(con, ps, null);
        }
    }


    /**
     * 更新部门信息
     * @param deptId 部门ID
     * @param deptName 新的部门名称
     * @return 更新成功返回true，如果部门名称已存在或其他错误则返回false
     */
    public boolean updateDepartment(long deptId, String deptName) {
        // 定义SQL更新语句
        String sql = "UPDATE department SET dept_name = ? WHERE dept_id = ?";
        Connection con = null;
        PreparedStatement ps = null;
        try {
            // 获取数据库连接
            con = SqlUtil.getConnection();
            if (con == null) {
                return false;
            }
            // 创建预处理语句
            ps = con.prepareStatement(sql);
            // 设置参数
            ps.setString(1, deptName);
            ps.setLong(2, deptId);
            // 执行更新并返回结果
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            // 关闭资源
            SqlUtil.closeAll(con, ps, null);
        }
    }


    /**
     * 删除部门
     * @param deptId 要删除的部门ID
     * @return 删除成功返回true，如果存在外键约束或其他错误则返回false
     */
    public boolean deleteDepartment(long deptId) {
        // 定义SQL删除语句
        String sql = "DELETE FROM department WHERE dept_id = ?";
        Connection con = null;
        PreparedStatement ps = null;
        try {
            // 获取数据库连接
            con = SqlUtil.getConnection();
            if (con == null) {
                return false;
            }
            // 创建预处理语句
            ps = con.prepareStatement(sql);
            // 设置参数
            ps.setLong(1, deptId);
            // 执行更新并返回结果
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            // 有外键引用时会失败
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            // 关闭资源
            SqlUtil.closeAll(con, ps, null);
        }
    }
}