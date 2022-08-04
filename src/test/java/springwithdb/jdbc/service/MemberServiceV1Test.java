package springwithdb.jdbc.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import springwithdb.jdbc.connection.ConnectionConst;
import springwithdb.jdbc.domain.Member;
import springwithdb.jdbc.repository.MemberRepositoryV1;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static springwithdb.jdbc.connection.ConnectionConst.*;

/**
 * 기본 동작, 트랜잭션이 없어서 문제 발생
 */
class MemberServiceV1Test {
    public static final String Member_A = "memberA";
    public static final String Member_B = "memberB";
    public static final String Member_EX = "ex";

    private MemberRepositoryV1 memberRepository;
    private MemberServiceV1 memberService;

    @BeforeEach
    void before() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        memberRepository = new MemberRepositoryV1(dataSource);
        memberService = new MemberServiceV1(memberRepository);
    }

    @AfterEach
    void after() throws SQLException {
        memberRepository.delete(Member_A);
        memberRepository.delete(Member_B);
        memberRepository.delete(Member_EX);
    }
    @Test
    @DisplayName("Transfer Success")
    void accountTransfer() throws SQLException {
        //given
        Member memberA = new Member(Member_A, 10000);
        Member memberB = new Member(Member_B, 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberB);

        //when
        memberService.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), 2000);

        //then
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberB = memberRepository.findById(memberB.getMemberId());

        assertThat(findMemberA.getMoney()).isEqualTo(8000);
        assertThat(findMemberB.getMoney()).isEqualTo(12000);
    }

    @Test
    @DisplayName("Transfer Exception")
    void accountTransferEX() throws SQLException {
        //given
        Member memberA = new Member(Member_A, 10000);
        Member memberEX = new Member(Member_EX, 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberEX);

        //when
        assertThatThrownBy(() -> memberService.accountTransfer(memberA.getMemberId(), memberEX.getMemberId(), 2000))
                .isInstanceOf(IllegalStateException.class);

        //then
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberB = memberRepository.findById(memberEX.getMemberId());

        assertThat(findMemberA.getMoney()).isEqualTo(8000);
        assertThat(findMemberB.getMoney()).isEqualTo(10000);
    }
}