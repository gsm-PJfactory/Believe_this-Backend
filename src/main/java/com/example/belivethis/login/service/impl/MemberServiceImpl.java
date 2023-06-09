package com.example.belivethis.login.service.impl;

import com.example.belivethis.login.data.dto.MemberSignUpRequestDTO;
import com.example.belivethis.login.data.entity.MemberEntity;
import com.example.belivethis.login.data.repository.MemberRepository;
import com.example.belivethis.login.jwt.JwtTokenProvider;
import com.example.belivethis.login.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    @Override
    public Long signUp(MemberSignUpRequestDTO requestDto) throws Exception {

        if (memberRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new Exception("이미 존재하는 이메일입니다.");
        }

        if (!requestDto.getPassword().equals(requestDto.getCheckedPassword())) {
            throw new Exception("비밀번호가 일치하지 않습니다.");
        }

        MemberEntity member = memberRepository.save(requestDto.toEntity());
        member.encodePassword(passwordEncoder);

        member.addUserAuthority();
        return member.getId();
    }

    @Override
    public String login(Map<String, String> members) {

        MemberEntity member = (MemberEntity) memberRepository.findByEmail(members.get("email"))
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 Email 입니다."));

        String password = members.get("password");
        if (!member.checkPassword(passwordEncoder, password)) {
            throw new IllegalArgumentException("잘못된 비밀번호입니다.");
        }

        List<String> roles = new ArrayList<>();
        roles.add(member.getRole().name());

        return jwtTokenProvider.createToken(member.getUsername(), roles);

    }
}
